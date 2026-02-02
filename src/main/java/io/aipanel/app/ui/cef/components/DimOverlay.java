package io.aipanel.app.ui.cef.components;

import io.aipanel.app.ui.Theme;

import javax.swing.*;
import java.awt.*;

public class DimOverlay extends JPanel {

    private float alpha = 0f;
    private float targetAlpha = 0f;
    private Timer dimTimer;

    public DimOverlay() {
        setOpaque(false);
        setVisible(false);

        dimTimer = new Timer(16, e -> tick());
    }

    public void show() {
        targetAlpha = 0.35f;
        setVisible(true);
        if(dimTimer == null) {
            dimTimer = new Timer(16, e -> tick());
        }
        dimTimer.start();
    }

    public void hide() {
        targetAlpha = 0f;
        if(dimTimer == null) {
            dimTimer = new Timer(16, e -> tick());
        }
        dimTimer.start();
    }

    private void tick() {
        float diff = targetAlpha - alpha;

        if (Math.abs(diff) < 0.01f) {
            alpha = targetAlpha;
            dimTimer.stop();

            if (alpha <= 0f) {
                setVisible(false);
            }
        } else {
            alpha += diff * 0.22f;
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        if (alpha <= 0.01f) return;

        var g = (Graphics2D) g0;
        g.setColor(Theme.withAlpha(new Color(0, 0, 0), alpha));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}