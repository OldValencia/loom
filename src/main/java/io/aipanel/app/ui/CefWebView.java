package io.aipanel.app.ui;

import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class CefWebView extends JPanel {

    private CefClient client;
    private CefBrowser browser;

    private final String LOG_DIR = System.getProperty("user.home") + File.separator + ".aipanel";

    public CefWebView(String startUrl) {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_DEEP);
        initCef(startUrl);
    }

    private void initCef(String startUrl) {
        try {
            var builder = new CefAppBuilder();
            builder.setInstallDir(new File(LOG_DIR, "jcef-bundle"));
            builder.getCefSettings().windowless_rendering_enabled = false;

            var app = builder.build();
            client = app.createClient();
            browser = client.createBrowser(startUrl, false, false);

            add(browser.getUIComponent(), BorderLayout.CENTER);
            revalidate();
        } catch (IOException | UnsupportedPlatformException | InterruptedException | CefInitializationException e) {
            e.printStackTrace();// todo fixme
        }
    }

    public void loadUrl(String url) {
        if (browser != null) {
            browser.loadURL(url);
        }
    }

    public void dispose() {
        if (browser != null) {
            browser.close(true);
        }
        if (client != null) {
            client.dispose();
        }
    }
}