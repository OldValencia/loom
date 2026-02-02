package io.aipanel.app.ui;

import io.aipanel.app.config.AiConfiguration;
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
import org.cef.handler.CefContextMenuHandlerAdapter;
import org.cef.handler.CefKeyboardHandlerAdapter;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.cef.misc.BoolRef;
import org.cef.network.CefCookieManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

@Slf4j
public class CefWebView extends JPanel {

    private final String BASE_DIR = new File(System.getProperty("user.home"), ".aipanel").getAbsolutePath();
    private final String LOGS_DIR = LogSetup.LOGS_DIR;
    private final String INSTALL_DIR = new File(BASE_DIR, "jcef-bundle").getAbsolutePath();
    private final String CACHE_DIR = new File(BASE_DIR, "cache").getAbsolutePath();
    private final String CEF_LOG_FILE = new File(LOGS_DIR, "cef.log").getAbsolutePath();

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
    private AiConfiguration.AiConfig currentConfig;

    private double currentZoomLevel = 0.0;
    private boolean zoomEnabled = true;

    // Слои для оверлеев
    private final JLayeredPane layeredPane;
    private final DimOverlay dimOverlay;
    private FadeOverlay fadeOverlay; // Создается и добавляется динамически

    @Setter
    private Consumer<Double> zoomCallback;

    public CefWebView(String startUrl) {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_DEEP);

        // Инициализируем JLayeredPane
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null); // Абсолютное позиционирование, мы будем менять размеры вручную
        add(layeredPane, BorderLayout.CENTER);

        // Затемнение
        dimOverlay = new DimOverlay();
        layeredPane.add(dimOverlay, JLayeredPane.PALETTE_LAYER); // Слой палитры (выше дефолтного)

        // Слушатель изменения размеров окна, чтобы оверлеи растягивались
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateLayerBounds();
            }
        });

        smartClean();
        initCef(startUrl);
    }

    private void updateLayerBounds() {
        int w = getWidth();
        int h = getHeight();

        // Растягиваем браузер
        if (browser != null) {
            Component browserUI = browser.getUIComponent();
            browserUI.setBounds(0, 0, w, h);
        }

        // Растягиваем оверлеи
        if (dimOverlay != null) dimOverlay.setBounds(0, 0, w, h);
        if (fadeOverlay != null) fadeOverlay.setBounds(0, 0, w, h);

        layeredPane.revalidate();
        layeredPane.repaint();
    }

    public void setCurrentConfig(AiConfiguration.AiConfig config) {
        this.currentConfig = config;
    }

    // === Управление затемнением (для SettingsWindow) ===
    public void showDim() {
        dimOverlay.fadeIn();
    }

    public void hideDim() {
        dimOverlay.fadeOut();
    }

    // === Сеттер для FadeOverlay (вызывается извне или создаем внутри) ===
    public void setFadeOverlay(FadeOverlay overlay) {
        this.fadeOverlay = overlay;
        // Добавляем на самый высокий слой (MODAL)
        layeredPane.add(fadeOverlay, JLayeredPane.MODAL_LAYER);
        updateLayerBounds();
    }

    public void restart() {
        if (browser != null) {
            String url = (currentConfig != null) ? currentConfig.url() : browser.getURL();

            // Удаляем компонент браузера из слоя
            layeredPane.remove(browser.getUIComponent());

            browser.close(true);
            browser = null;

            if (client != null) {
                browser = client.createBrowser(url, false, false);
                // Добавляем браузер на самый нижний слой
                Component browserUI = browser.getUIComponent();
                layeredPane.add(browserUI, JLayeredPane.DEFAULT_LAYER);
            }

            updateLayerBounds(); // Принудительно обновляем размеры

            if (fadeOverlay != null) {
                fadeOverlay.setVisible(true);
                fadeOverlay.startFadeOut();
            }
        }
    }

    // ... smartClean, deleteDirectory остаются без изменений ...
    private void smartClean() {
        // (код очистки такой же, как был)
        try {
            File bundleDir = new File(INSTALL_DIR);
            if (bundleDir.exists()) {
                Files.walk(bundleDir.toPath())
                        .filter(p -> p.toFile().getName().equals("locales") && p.toFile().isDirectory())
                        .findFirst()
                        .ifPresent(localesPath -> {
                            File[] files = localesPath.toFile().listFiles();
                            if (files != null) {
                                for (File f : files) {
                                    String name = f.getName();
                                    if (!name.equals("en-US.pak") && !name.equals("ru.pak")) {
                                        f.delete();
                                    }
                                }
                            }
                        });
            }
        } catch (Exception ignored) {}

        File cache = new File(CACHE_DIR);
        if (cache.exists()) {
            File[] files = cache.listFiles();
            if (files != null) {
                for (File f : files) {
                    String name = f.getName();
                    if (name.equals("Cache") || name.equals("Code Cache") ||
                            name.equals("GPUCache") || name.equals("ScriptCache")) {
                        deleteDirectory(f);
                    }
                }
            }
        }
    }

    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] entries = dir.listFiles();
            if (entries != null) {
                for (File entry : entries) deleteDirectory(entry);
            }
        }
        dir.delete();
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

            browser = client.createBrowser(startUrl, false, false);

            // Добавляем браузер в нижний слой
            Component browserUI = browser.getUIComponent();
            layeredPane.add(browserUI, JLayeredPane.DEFAULT_LAYER);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try { CefApp.getInstance().dispose(); } catch (Exception ignored) {}
            }));

            // Первичный апдейт размеров (хотя окно еще мб 0x0)
            updateLayerBounds();
            log.info("JCEF Initialized");

        } catch (IOException | UnsupportedPlatformException | InterruptedException | CefInitializationException e) {
            log.error("Failed to init JCEF", e);
        }
    }

    private void configureSettings(CefAppBuilder builder) {
        // (код настроек без изменений)
        var settings = builder.getCefSettings();
        settings.windowless_rendering_enabled = false;
        settings.cache_path = CACHE_DIR;
        settings.root_cache_path = CACHE_DIR;
        settings.persist_session_cookies = true;
        settings.log_file = CEF_LOG_FILE;
        settings.user_agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    }

    // ... Handlers (Zoom, Keyboard, ContextMenu) без изменений ...
    private void setupZoomHandler() {
        var msgRouter = CefMessageRouter.create();
        msgRouter.addHandler(new CefMessageRouterHandlerAdapter() {
            @Override
            public boolean onQuery(CefBrowser browser, CefFrame frame, long queryId, String request, boolean persistent, CefQueryCallback callback) {
                if (request.startsWith("zoom_scroll:") && zoomEnabled) {
                    try {
                        var delta = Double.parseDouble(request.split(":")[1]);
                        changeZoom(delta < 0);
                    } catch (Exception ignored) {}
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
                if (!zoomEnabled || (event.modifiers & 2) == 0) return false;
                boolean isPressed = event.type == CefKeyEvent.EventType.KEYEVENT_RAWKEYDOWN;
                int code = event.windows_key_code;
                if (code == 187 || code == 61 || code == 107) { if (isPressed) changeZoom(true); return true; }
                if (code == 189 || code == 45 || code == 109) { if (isPressed) changeZoom(false); return true; }
                if (code == 48 || code == 96) { if (isPressed) resetZoom(); return true; }
                return false;
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

    private void setupLoadHandler() {
        client.addLoadHandler(new CefLoadHandlerAdapter() {
            @Override
            public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
                if (frame.isMain()) {
                    browser.executeJavaScript(ZOOM_JS, frame.getURL(), 0);
                    browser.setZoomLevel(currentZoomLevel);
                    if (fadeOverlay != null) {
                        SwingUtilities.invokeLater(() -> fadeOverlay.startFadeOut());
                    }
                }
            }
        });
    }

    // ... Публичные API методы (те же) ...
    public void setZoomEnabled(boolean enabled) {
        this.zoomEnabled = enabled;
        if (!enabled) resetZoom();
    }

    public void resetZoom() {
        setZoomInternal(0.0);
    }

    public void loadUrl(String url) {
        if (browser != null) browser.loadURL(url);
    }

    public void clearCookies() {
        CefCookieManager.getGlobalManager().deleteCookies("", "");
    }

    public void shutdown(Runnable onComplete) {
        CefCookieManager.getGlobalManager().flushStore(new org.cef.callback.CefCompletionCallback() {
            @Override
            public void onComplete() {
                dispose();
                onComplete.run();
            }
        });
    }

    public void dispose() {
        if (browser != null) browser.close(true);
        if (client != null) client.dispose();
    }

    private void changeZoom(boolean increase) {
        double step = 0.5;
        double newLevel = currentZoomLevel + (increase ? step : -step);
        newLevel = Math.max(-3.0, Math.min(4.0, newLevel));
        setZoomInternal(newLevel);
    }

    private void setZoomInternal(double level) {
        this.currentZoomLevel = level;
        if (browser != null) browser.setZoomLevel(level);
        if (zoomCallback != null) {
            double percentage = Math.pow(1.2, level) * 100;
            long displayVal = Math.round(percentage / 5.0) * 5;
            zoomCallback.accept((double) displayVal);
        }
    }
}