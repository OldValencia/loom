package io.aipanel.app.ui.cef.components;

import io.aipanel.app.ui.Theme;

import javax.swing.*;
import java.awt.*;

public class FadeOverlay extends JPanel {

    private float alpha = 0f;
    private float targetAlpha = 0f;
    private final Timer fadeTimer;
    private Runnable onFadeComplete;

    public FadeOverlay() {
        setOpaque(false);
        setVisible(false);

        fadeTimer = new Timer(16, e -> tick());
    }

    public void fadeInThen(Runnable action) {
        this.onFadeComplete = action;
        targetAlpha = 1f;
        setVisible(true);
        fadeTimer.start();
    }

    public void fadeOut() {
        targetAlpha = 0f;
        fadeTimer.start();
    }

    private void tick() {
        float diff = targetAlpha - alpha;

        if (Math.abs(diff) < 0.02f) {
            alpha = targetAlpha;
            fadeTimer.stop();

            if (alpha >= 1f && onFadeComplete != null) {
                onFadeComplete.run();
                onFadeComplete = null;
                fadeOut();
            } else if (alpha <= 0f) {
                setVisible(false);
            }
        } else {
            alpha += diff * 0.25f;
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        if (alpha <= 0.01f) return;

        var g = (Graphics2D) g0;
        g.setColor(Theme.withAlpha(Theme.BG_DEEP, alpha));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}