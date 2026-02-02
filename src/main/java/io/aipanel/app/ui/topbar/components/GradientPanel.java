package io.aipanel.app.ui.topbar.components;

import io.aipanel.app.ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class GradientPanel extends JPanel {

    private Color accentColor = Theme.ACCENT;
    private LinearGradientPaint cachedGradientLeft;
    private LinearGradientPaint cachedGradientFade;

    public GradientPanel() {
        setOpaque(false);
        rebuildGradients();
    }

    public void setAccentColor(Color c) {
        this.accentColor = c;
        rebuildGradients();
        repaint();
    }

    public void updateAccentColor(Color c) {
        setAccentColor(c);
    }

    private void rebuildGradients() {
        var cachedTransparent = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 0);
        var cachedFaded = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 40);

        cachedGradientLeft = new LinearGradientPaint(
                new Point(0, 0), new Point(50, 0),
                new float[]{0f, 1f},
                new Color[]{cachedTransparent, cachedFaded}
        );

        var fadeWidth = (int) (820 * 0.20);
        cachedGradientFade = new LinearGradientPaint(
                new Point(50, 0), new Point(50 + fadeWidth, 0),
                new float[]{0f, 1f},
                new Color[]{cachedFaded, cachedTransparent}
        );
    }

    @Override
    protected void paintComponent(Graphics g0) {
        var g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        var w = getWidth();
        var h = getHeight();

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, w, h);

        g.setColor(Theme.BG_BAR);
        var shape = new RoundRectangle2D.Float(0, 0, w, h, 14, 14);
        g.fill(shape);

        g.setComposite(AlphaComposite.SrcOver);
        var oldClip = g.getClip();
        g.setClip(shape);

        g.setPaint(cachedGradientLeft);
        g.fillRect(0, 0, 50, h);

        var fadeWidth = (int) (w * 0.20);
        g.setPaint(cachedGradientFade);
        g.fillRect(50, 0, fadeWidth, h);

        g.setClip(oldClip);

        g.setColor(Theme.BORDER);
        g.setStroke(new BasicStroke(1f));
        g.drawLine(0, h - 1, w, h - 1);
    }
}
