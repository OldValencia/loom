package io.loom.app.ui.settings.components;

import io.loom.app.ui.Theme;

import javax.swing.*;

class ProvidersEmptyListLabel extends JLabel {
    ProvidersEmptyListLabel() {
        super("No providers available. Click '+ Add' to create a custom one.");
        this.setFont(Theme.FONT_SETTINGS.deriveFont(12f));
        this.setForeground(Theme.TEXT_TERTIARY);
        this.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
    }
}
