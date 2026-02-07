package io.loom.app.ui.settings.components;

import io.loom.app.ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class ProvidersListTextButton extends JButton {
    ProvidersListTextButton(String text, Color color, ActionListener action) {
        super(text);
        this.setFont(Theme.FONT_SETTINGS.deriveFont(12f));
        this.setForeground(color);
        this.setOpaque(false);
        this.setContentAreaFilled(false);
        this.setBorderPainted(false);
        this.setFocusPainted(false);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.addActionListener(action);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (color == Theme.TEXT_SECONDARY) setForeground(Theme.ACCENT);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setForeground(color);
            }
        });
    }
}
