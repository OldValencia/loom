package io.aipanel.app.ui;

import io.aipanel.app.config.AppPreferences;
import io.aipanel.app.ui.cef.components.DimOverlay;
import io.aipanel.app.ui.cef.components.FadeOverlay;
import io.aipanel.app.utils.LogSetup;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefContextMenuParams;
import org.cef.callback.CefMenuModel;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.*;
import org.cef.misc.BoolRef;
import org.cef.network.CefCookieManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

@Slf4j
public class CefWebView extends JPanel {

    private static final String BASE_DIR = new File(System.getProperty("user.home"), ".aipanel").getAbsolutePath();
    private static final String INSTALL_DIR = new File(BASE_DIR, "jcef-bundle").getAbsolutePath();
    private static final String CACHE_DIR = new File(BASE_DIR, "cache").getAbsolutePath();
    private static final String CEF_LOG_FILE = new File(LogSetup.LOGS_DIR, "cef.log").getAbsolutePath();

    private static final String ZOOM_JS = """
            document.addEventListener('wheel', function(e) {
               if(e.ctrlKey) {
                   e.preventDefault();
                   window.cefQuery({request: 'zoom_scroll:' + e.deltaY});
               }
            }, {passive: false});
            """;

    private CefClient client;
    private CefBrowser browser;

    @Setter
    private Consumer<Double> zoomCallback;

    private final AppPreferences appPreferences;
    private final FadeOverlay fadeOverlay;
    private final DimOverlay dimOverlay;

    public CefWebView(String startUrl, AppPreferences appPreferences) {
        this.appPreferences = appPreferences;

        setLayout(null);
        setBackground(Theme.BG_DEEP);

        fadeOverlay = new FadeOverlay();
        dimOverlay = new DimOverlay();

        initCef(startUrl);

        add(fadeOverlay);
        add(dimOverlay);
    }

    @Override
    public void doLayout() {
        super.doLayout();
        int w = getWidth();
        int h = getHeight();

        if (fadeOverlay != null) fadeOverlay.setBounds(0, 0, w, h);
        if (dimOverlay != null) dimOverlay.setBounds(0, 0, w, h);

        for (var comp : getComponents()) {
            if (comp != fadeOverlay && comp != dimOverlay) {
                comp.setBounds(0, 0, w, h);
            }
        }
    }

    public void setZoomEnabled(boolean enabled) {
        appPreferences.setZoomEnabled(enabled);
        if (!appPreferences.isZoomEnabled()) {
            resetZoom();
        }
    }

    public void resetZoom() {
        setZoomInternal(0.0);
    }

    public void loadUrl(String url) {
        if (browser != null) {
            fadeOverlay.fadeInThen(() -> browser.loadURL(url));
        }
    }

    public void showDim() {
        dimOverlay.show();
    }

    public void hideDim() {
        dimOverlay.hide();
    }

    public void clearCookies() {
        CefCookieManager.getGlobalManager().deleteCookies("", "");
    }

    public void shutdown(Runnable onComplete) {
        CefCookieManager.getGlobalManager().flushStore(() -> {
            dispose();
            onComplete.run();
        });
    }

    public void dispose() {
        if (browser != null) {
            browser.close(true);
        }
        if (client != null) {
            client.dispose();
        }
    }

    private void initCef(String startUrl) {
        try {
            var builder = new CefAppBuilder();
            builder.setInstallDir(new File(INSTALL_DIR));
            configureSettings(builder);

            var app = builder.build();
            client = app.createClient();

            setupZoomHandler();
            setupKeyboardHandler();
            setupLoadHandler();
            setupContextMenuHandler();
            setupNotificationHandler();

            browser = client.createBrowser(startUrl, false, false);
            add(browser.getUIComponent(), 0);

            Runtime.getRuntime().addShutdownHook(new Thread(this::performShutdown));

            revalidate();
            log.info("JCEF initialized successfully");
        } catch (IOException | UnsupportedPlatformException | InterruptedException | CefInitializationException e) {
            log.error("Failed to initialize JCEF", e);
        }
    }

    private void configureSettings(CefAppBuilder builder) {
        var settings = builder.getCefSettings();
        settings.windowless_rendering_enabled = false;
        settings.cache_path = CACHE_DIR;
        settings.root_cache_path = CACHE_DIR;
        settings.persist_session_cookies = true;
        settings.log_file = CEF_LOG_FILE;
        settings.log_severity = org.cef.CefSettings.LogSeverity.LOGSEVERITY_WARNING;
        settings.user_agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    }

    private void setupZoomHandler() {
        var msgRouter = CefMessageRouter.create();
        msgRouter.addHandler(new CefMessageRouterHandlerAdapter() {
            @Override
            public boolean onQuery(CefBrowser browser, CefFrame frame, long queryId, String request, boolean persistent, CefQueryCallback callback) {
                if (request.startsWith("zoom_scroll:") && appPreferences.isZoomEnabled()) {
                    try {
                        var delta = Double.parseDouble(request.split(":")[1]);
                        changeZoom(delta < 0);
                    } catch (Exception e) {
                        log.error("Can't parse zoom request", e);
                    }
                    return true;
                }
                return false;
            }
        }, true);
        client.addMessageRouter(msgRouter);
    }

    private void setupKeyboardHandler() {
        client.addKeyboardHandler(new CefKeyboardHandlerAdapter() {
            @Override
            public boolean onPreKeyEvent(CefBrowser browser, CefKeyEvent event, BoolRef is_keyboard_shortcut) {
                if (!appPreferences.isZoomEnabled() || (event.modifiers & 2) == 0) return false; // Not Ctrl

                boolean isPressed = event.type == CefKeyEvent.EventType.KEYEVENT_RAWKEYDOWN;
                int code = event.windows_key_code;

                // Zoom In (+, =, Numpad +)
                if (code == 187 || code == 61 || code == 107) {
                    if (isPressed) changeZoom(true);
                    return true;
                }
                // Zoom Out (-, _, Numpad -)
                if (code == 189 || code == 45 || code == 109) {
                    if (isPressed) changeZoom(false);
                    return true;
                }
                // Reset (0, Numpad 0)
                if (code == 48 || code == 96) {
                    if (isPressed) resetZoom();
                    return true;
                }
                return false;
            }
        });
    }

    private void setupLoadHandler() {
        client.addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
                if (frame.isMain()) {
                    browser.executeJavaScript(ZOOM_JS, frame.getURL(), 0);
                    browser.setZoomLevel(appPreferences.getLastZoomValue());
                    setZoomInternal(appPreferences.getLastZoomValue());
                }
            }
        });
    }

    private void setupContextMenuHandler() {
        client.addContextMenuHandler(new CefContextMenuHandlerAdapter() {
            @Override
            public void onBeforeContextMenu(CefBrowser browser, CefFrame frame, CefContextMenuParams params, CefMenuModel model) {
                model.clear();
            }
        });
    }

    private void setupNotificationHandler() {
        client.addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public boolean onConsoleMessage(CefBrowser browser, org.cef.CefSettings.LogSeverity level, String message, String source, int line) {
                if (message.contains("Notification") || message.contains("notification")) {
                    log.info("Notification detected: {}", message);
                    showSystemNotification("AI Panel", message);
                }
                return false;
            }
        });
    }

    private void showSystemNotification(String title, String message) {
        if (SystemTray.isSupported()) {
            try {
                var tray = SystemTray.getSystemTray();
                var image = Toolkit.getDefaultToolkit().createImage("icon.png");
                var trayIcon = new TrayIcon(image, "AI Panel");
                trayIcon.setImageAutoSize(true);
                trayIcon.setToolTip("AI Panel");

                if (tray.getTrayIcons().length == 0) {
                    tray.add(trayIcon);
                } else {
                    trayIcon = tray.getTrayIcons()[0];
                }

                trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
            } catch (Exception e) {
                log.error("Failed to show system notification", e);
            }
        }
    }

    private void performShutdown() {
        log.info("Shutting down JCEF");
        try {
            CefApp.getInstance().dispose();
        } catch (Exception e) {
            log.error("Exception during JCEF shutdown", e);
        }
    }

    private void changeZoom(boolean increase) {
        double step = 0.5;
        double newLevel = appPreferences.getLastZoomValue() + (increase ? step : -step);
        newLevel = Math.max(-3.0, Math.min(4.0, newLevel));
        setZoomInternal(newLevel);
    }

    private void setZoomInternal(double level) {
        appPreferences.setLastZoomValue(level);
        if (browser != null) {
            browser.setZoomLevel(level);
        }
        if (zoomCallback != null) {
            double percentage = Math.pow(1.2, level) * 100;
            long displayVal = Math.round(percentage / 5.0) * 5;
            zoomCallback.accept((double) displayVal);
        }
    }
}