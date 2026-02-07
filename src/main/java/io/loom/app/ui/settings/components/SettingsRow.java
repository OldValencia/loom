package io.loom.app.ui.settings.components;

import io.loom.app.ui.Theme;

import javax.swing.*;
import java.awt.*;

public class SettingsRow extends JPanel {
    public SettingsRow(String labelText, JComponent control) {
        super();
        this.setOpaque(false);
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));

        this.add(buildRowLabel(labelText));
        this.add(Box.createHorizontalGlue());
        this.add(control);
    }

    private static JLabel buildRowLabel(String labelText) {
        var label = new JLabel(labelText);
        label.setFont(Theme.FONT_SETTINGS);
        label.setForeground(Theme.TEXT_PRIMARY);
        return label;
    }
}
