package io.loom.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.cef.browser.CefBrowser;

import javax.swing.*;
import java.io.File;

import static io.loom.app.utils.LogSetup.LOGS_DIR;

@Slf4j
public class MemoryMonitor {

    private static final long HIGH_MEMORY_THRESHOLD = 400 * 1024 * 1024; // 400 MB
    private static final long CRITICAL_MEMORY_THRESHOLD = 450 * 1024 * 1024; // 450 MB
    private static final int CHECK_INTERVAL = 30000; // 30s
    private static final int HIGH_MEMORY_CHECK_INTERVAL = 10000; // 10s when memory is high

    private final CefBrowser browser;
    private final Timer timer;
    private boolean isHighMemoryMode = false;

    public MemoryMonitor(CefBrowser browser) {
        this.browser = browser;
        this.timer = new Timer(CHECK_INTERVAL, e -> checkMemory());
    }

    public void start() {
        timer.start();
        log.info("Memory monitoring started");
    }

    public void stop() {
        timer.stop();
        log.info("Memory monitoring stopped");
    }

    private void checkMemory() {
        var runtime = Runtime.getRuntime();
        var totalMemory = runtime.totalMemory();
        var freeMemory = runtime.freeMemory();
        var usedMemory = totalMemory - freeMemory;
        var maxMemory = runtime.maxMemory();
        var usedPercent = (usedMemory * 100.0) / maxMemory;

        log.debug("Memory: {} MB / {} MB ({}%)",
                usedMemory / 1024 / 1024,
                maxMemory / 1024 / 1024,
                String.format("%.1f", usedPercent)
        );

        // Switch to high frequency monitoring if memory is high
        if (usedMemory > HIGH_MEMORY_THRESHOLD && !isHighMemoryMode) {
            isHighMemoryMode = true;
            timer.setDelay(HIGH_MEMORY_CHECK_INTERVAL);
            log.warn("Entering high memory mode, increasing monitoring frequency");
        } else if (usedMemory < HIGH_MEMORY_THRESHOLD * 0.8 && isHighMemoryMode) {
            isHighMemoryMode = false;
            timer.setDelay(CHECK_INTERVAL);
            log.info("Exiting high memory mode, reducing monitoring frequency");
        }

        if (usedMemory > HIGH_MEMORY_THRESHOLD) {
            log.warn("High memory usage detected: {} MB", usedMemory / 1024 / 1024);
            performCleanup(usedMemory > CRITICAL_MEMORY_THRESHOLD);
        }
    }

    private void performCleanup(boolean critical) {
        log.info("Performing {} memory cleanup...", critical ? "CRITICAL" : "normal");

        // Clear icon cache if critical
        if (critical) {
            try {
                var aidockClass = Class.forName("io.loom.app.ui.topbar.components.AiDock");
                var clearMethod = aidockClass.getDeclaredMethod("clearIconCache");
                clearMethod.invoke(null);
                log.info("Icon cache cleared due to critical memory pressure");
            } catch (Exception e) {
                log.warn("Could not clear icon cache", e);
            }
        }

        if (critical) {
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.gc();

        if (browser != null && !browser.isLoading()) {
            browser.executeJavaScript(
                    "if (window.gc) { window.gc(); }" +
                    "if (window.CollectGarbage) { window.CollectGarbage(); }",
                    "", 0
            );
        }

        clearApplicationTrash();

        var runtime = Runtime.getRuntime();
        var afterCleanup = runtime.totalMemory() - runtime.freeMemory();
        log.info("Memory after cleanup: {} MB", afterCleanup / 1024 / 1024);
    }

    private void clearApplicationTrash() {
        var logsDir = new File(LOGS_DIR);
        LogSetup.rotateCefLog(logsDir);
    }
}
