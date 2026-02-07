package io.loom.app.ui.dialogs.components;

import io.loom.app.ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ProvidersFormButtonsPanel extends JPanel {
    public ProvidersFormButtonsPanel(boolean isAddDialog, ActionListener actionConfirmed, ActionListener actionCancelled) {
        super(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        this.setOpaque(false);
        this.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        var cancelBtn = createButton("Cancel", false);
        cancelBtn.addActionListener(actionCancelled);

        var saveBtn = createButton(isAddDialog ? "Add" : "Save", true);
        saveBtn.addActionListener(actionConfirmed);

        this.add(cancelBtn);
        this.add(saveBtn);
    }

    private JButton createButton(String text, boolean primary) {
        var button = new JButton(text);
        button.setFont(Theme.FONT_SETTINGS);
        button.setForeground(primary ? Color.WHITE : Theme.TEXT_PRIMARY);
        button.setBackground(primary ? Theme.ACCENT : Theme.BG_POPUP);
        button.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(primary ? Theme.ACCENT.brighter() : Theme.BG_HOVER);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(primary ? Theme.ACCENT : Theme.BG_POPUP);
            }
        });

        return button;
    }
}
