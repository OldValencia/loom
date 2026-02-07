package io.loom.app.ui.dialogs.components;

import io.loom.app.ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ProviderMainPanel extends JPanel {
    public ProviderMainPanel() {
        super();
        this.setOpaque(false);
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
    }

    @Override
    protected void paintComponent(Graphics g) {
        var g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.BG_BAR);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
    }
}
