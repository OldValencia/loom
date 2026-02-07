package io.loom.app;

import io.loom.app.config.AiConfiguration;
import io.loom.app.config.AppPreferences;
import io.loom.app.utils.LogSetup;
import io.loom.app.windows.MainWindow;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

public class LoomApplication {

    private static Logger log;

    public static void main(String[] args) {
        LogSetup.init();
        log = LoggerFactory.getLogger(LoomApplication.class);

        System.setProperty("sun.awt.exception.handler", AwtExceptionHandler.class.getName());

        setupCookies();

        var appPreferences = new AppPreferences();
        var aiConfiguration = new AiConfiguration(appPreferences);

        SwingUtilities.invokeLater(() -> {
            try {
                var mainWindow = new MainWindow(aiConfiguration, appPreferences);
                mainWindow.showWindow();
                log.info("Main window displayed.");
            } catch (Exception e) {
                log.error("Failed to show main window", e);
            }
        });
    }

    private static void setupCookies() {
        var manager = new CookieManager();
        manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(manager);
    }

    @Slf4j
    public static class AwtExceptionHandler {
        public void handle(Throwable t) {
            log.error("Critical AWT Error", t);
        }
    }
}
