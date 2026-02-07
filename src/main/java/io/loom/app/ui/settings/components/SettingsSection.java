package io.loom.app.ui.settings.components;

import io.loom.app.ui.Theme;

import javax.swing.*;
import java.awt.*;

public class SettingsSection extends JLabel {
    public SettingsSection(String title) {
        super(title.toUpperCase());
        this.setFont(Theme.FONT_SETTINGS_SECTION);
        this.setForeground(Theme.TEXT_TERTIARY);
        this.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
}
