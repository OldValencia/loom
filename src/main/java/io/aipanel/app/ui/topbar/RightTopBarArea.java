package io.aipanel.app.ui.topbar;

import io.aipanel.app.ui.CefWebView;
import io.aipanel.app.ui.Theme;
import lombok.RequiredArgsConstructor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;

@RequiredArgsConstructor
class RightTopBarArea {

    private final CefWebView cefWebView;

    JPanel buildRightArea() {
        var wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 12));
        wrapper.setOpaque(false);

        wrapper.add(new AnimatedIconButton("⚙", Theme.BTN_HOVER_SETTINGS, () -> {
        }));
        wrapper.add(new AnimatedIconButton("✕", Theme.BTN_HOVER_CLOSE, this::handleClose));

        return wrapper;
    }

    private void handleClose() {
        cefWebView.dispose();
        System.exit(0);
    }

    static class AnimatedIconButton extends JPanel {

        private static final int SIZE = 30;
        private static final int RING_R = 13;
        private static final int INTERVAL = 16;

        private final String icon;
        private final Color hoverColor;
        private final Runnable action;

        private float progress = 0f;   // 0 = default state, 1 = hovered state
        private boolean hovered = false;
        private final Timer animTimer;

        AnimatedIconButton(String icon, Color hoverColor, Runnable action) {
            this.icon = icon;
            this.hoverColor = hoverColor;
            this.action = action;

            setPreferredSize(new Dimension(SIZE, SIZE));
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            animTimer = new Timer(INTERVAL, e -> tick());

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    animTimer.start();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    animTimer.start();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    action.run();
                }
            });
        }

        private void tick() {
            float target = hovered ? 1f : 0f;
            float diff = target - progress;

            if (Math.abs(diff) < 0.035f) {
                progress = target;
                animTimer.stop();
            } else {
                progress += diff * 0.22f;
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            var g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            if (progress > 0.02f) {
                float alpha = progress * 0.6f;
                g.setColor(withAlpha(Theme.BTN_RING, alpha));
                g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Ellipse2D.Float(cx - RING_R, cy - RING_R, RING_R * 2, RING_R * 2));
            }

            g.setColor(lerp(Theme.TEXT_SECONDARY, hoverColor, progress));
            g.setFont(Theme.FONT_RIGHT_TOP_BAR_AREA);
            var fm = g.getFontMetrics();
            g.drawString(icon,
                    cx - fm.stringWidth(icon) / 2,
                    cy + fm.getAscent() / 2 - 2);
        }

        private static Color lerp(Color a, Color b, float t) {
            return new Color(
                    clamp((int) (a.getRed() + (b.getRed() - a.getRed()) * t)),
                    clamp((int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t)),
                    clamp((int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t))
            );
        }

        private static Color withAlpha(Color c, float alpha) {
            return new Color(c.getRed(), c.getGreen(), c.getBlue(), clamp((int) (alpha * 255)));
        }

        private static int clamp(int v) {
            return Math.max(0, Math.min(255, v));
        }
    }
}
