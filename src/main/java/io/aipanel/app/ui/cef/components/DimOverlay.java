package io.aipanel.app.ui.cef.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

public class DimOverlay extends JComponent {

    private float alpha = 0.0f;
    private static final float MAX_ALPHA = 0.6f;
    private Timer timer;

    public DimOverlay() {
        setVisible(false);
        setOpaque(false);
        setFocusable(false);
        addMouseListener(new MouseAdapter() {
        });
    }

    public void fadeIn() {
        if (isVisible() && alpha >= MAX_ALPHA) return;

        stopTimer();
        setVisible(true); // Включаем компонент

        timer = new Timer(20, e -> {
            alpha += 0.05f;
            if (alpha >= MAX_ALPHA) {
                alpha = MAX_ALPHA;
                stopTimer();
            }
            repaint();
        });
        timer.start();
    }

    public void fadeOut() {
        if (!isVisible()) return;

        stopTimer();
        timer = new Timer(20, e -> {
            alpha -= 0.05f;
            if (alpha <= 0.0f) {
                alpha = 0.0f;
                stopTimer();
                setVisible(false);
            }
            repaint();
        });
        timer.start();
    }

    private void stopTimer() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (alpha > 0) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0, 0, 0, (int) (alpha * 255)));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }
}