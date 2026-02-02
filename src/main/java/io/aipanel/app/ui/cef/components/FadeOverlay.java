package io.aipanel.app.ui.cef.components;

import io.aipanel.app.ui.Theme;

import javax.swing.*;
import java.awt.*;

public class FadeOverlay extends JComponent {

    private float alpha = 1.0f;
    private Timer timer;
    private final boolean isDark;

    public FadeOverlay() {
        this.isDark = true;
        setOpaque(false);
        setVisible(true);
        addMouseListener(new java.awt.event.MouseAdapter() {});
    }

    public void startFadeOut() {
        if (timer != null && timer.isRunning()) return;

        timer = new Timer(30, e -> {
            alpha -= 0.05f;
            if (alpha <= 0.0f) {
                alpha = 0.0f;
                timer.stop();
                setVisible(false);
            }
            repaint();
        });
        Timer delay = new Timer(500, e -> timer.start());
        delay.setRepeats(false);
        delay.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (alpha > 0.01f) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(Theme.BG_DEEP);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }
}