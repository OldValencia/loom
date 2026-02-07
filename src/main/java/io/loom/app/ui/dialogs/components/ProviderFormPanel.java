package io.loom.app.ui.dialogs.components;

import io.loom.app.config.AiConfiguration;
import io.loom.app.ui.Theme;

import javax.swing.*;
import java.awt.*;

public class ProviderFormPanel extends JPanel {

    private final JTextField nameField;
    private final JTextField urlField;

    public ProviderFormPanel(AiConfiguration.AiConfig provider) {
        super();
        this.setOpaque(false);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.add(createLabel("Provider Name:"));
        nameField = createTextField(provider != null ? provider.name() : "");
        this.add(nameField);
        this.add(Box.createVerticalStrut(12));

        this.add(createLabel("Website URL:"));
        urlField = createTextField(provider != null ? provider.url() : "https://");
        this.add(urlField);
        this.add(Box.createVerticalStrut(8));

        var hintLabel = new JLabel("<html><i>Icon and color will be automatically extracted from the website</i></html>");
        hintLabel.setFont(Theme.FONT_SETTINGS.deriveFont(11f));
        hintLabel.setForeground(Theme.TEXT_TERTIARY);
        hintLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.add(hintLabel);
    }

    public String getNameFieldValue() {
        return nameField.getText().trim();
    }

    public String getUrlFieldValue() {
        return urlField.getText().trim();
    }

    private JLabel createLabel(String text) {
        var label = new JLabel(text);
        label.setFont(Theme.FONT_SETTINGS);
        label.setForeground(Theme.TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return label;
    }

    private JTextField createTextField(String text) {
        var field = new JTextField(text);
        field.setFont(Theme.FONT_SETTINGS);
        field.setForeground(Theme.TEXT_PRIMARY);
        field.setBackground(Theme.BG_POPUP);
        field.setCaretColor(Theme.TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }
}
