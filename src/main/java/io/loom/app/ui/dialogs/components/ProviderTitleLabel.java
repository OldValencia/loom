package io.loom.app.ui.dialogs.components;

import io.loom.app.ui.Theme;

import javax.swing.*;
import java.awt.*;

public class ProviderTitleLabel extends JLabel {
    public ProviderTitleLabel(boolean isAddDialog) {
        super(isAddDialog ? "Add New AI Provider" : "Edit AI Provider");
        this.setFont(Theme.FONT_SETTINGS.deriveFont(Font.BOLD, 18f));
        this.setForeground(Theme.TEXT_PRIMARY);
        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
    }
}
