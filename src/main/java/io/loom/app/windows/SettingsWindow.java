package io.loom.app.windows;

import io.loom.app.ui.settings.SettingsPanel;
import io.loom.app.utils.SystemUtils;
import lombok.RequiredArgsConstructor;

import javax.swing.*;

@RequiredArgsConstructor
public class SettingsWindow {

    private static final int TOPBAR_HEIGHT = 48;
    private static final float LERP_SPEED = 0.25f;

    private final JFrame owner;
    private final SettingsPanel settingsPanel;
    private JWindow window;

    private float progress = 0f;
    private float targetProgress = 0f;
    private Timer animTimer;

    public void open() {
        if (window == null) {
            createWindow();
        }

        settingsPanel.setSize(owner.getWidth(), Integer.MAX_VALUE);
        settingsPanel.doLayout();
        int targetHeight = settingsPanel.getPreferredSize().height;
        targetProgress = 1f;

        int x = owner.getX();
        int y = owner.getY() + TOPBAR_HEIGHT;
        int w = owner.getWidth();
        window.setBounds(x, y, w, targetHeight);
        window.setVisible(true);

        if (SystemUtils.isMac()) {
            SwingUtilities.invokeLater(() -> {
                window.toFront();
                window.repaint();
            });
        }

        animTimer.start();
    }

    public void close() {
        if (window == null) {
            return;
        }

        targetProgress = 0f;
        tick();
        animTimer.start();
    }

    public boolean isOpen() {
        return window != null && window.isVisible() && targetProgress > 0.5f;
    }

    private void createWindow() {
        window = new JWindow(owner);
        window.setAlwaysOnTop(true);
        window.setFocusableWindowState(false);
        window.setBackground(settingsPanel.getBackground());
        window.setContentPane(settingsPanel);

        animTimer = new Timer(10, e -> tick());
        progress = 0f;
        targetProgress = 0f;
    }

    private void tick() {
        float diff = targetProgress - progress;

        if (Math.abs(diff) < 0.02f) {
            progress = targetProgress;
            animTimer.stop();

            if (progress <= 0f) {
                window.setVisible(false);
            }
        } else {
            progress += diff * LERP_SPEED;
        }

        settingsPanel.setOpaque(progress > 0.01f);

        if (SystemUtils.isMac()) {
            window.repaint();
        } else {
            settingsPanel.repaint();
        }
    }
}