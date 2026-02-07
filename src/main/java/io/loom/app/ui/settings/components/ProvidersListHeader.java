package io.loom.app.ui.settings.components;

import io.loom.app.ui.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

class ProvidersListHeader extends JPanel {

    ProvidersListHeader(ActionListener action) {
        super(new BorderLayout());
        this.setOpaque(false);

        var titleLabel = new JLabel("AI PROVIDERS");
        titleLabel.setFont(Theme.FONT_SETTINGS_SECTION);
        titleLabel.setForeground(Theme.TEXT_TERTIARY);

        var addButton = new ProvidersListTextButton("+ Add", Theme.ACCENT, action);

        this.add(titleLabel, BorderLayout.WEST);
        this.add(addButton, BorderLayout.EAST);
    }
}
