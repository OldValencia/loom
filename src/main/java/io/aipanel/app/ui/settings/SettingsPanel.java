package io.aipanel.app.ui.settings;

import io.aipanel.app.config.AppPreferences;
import io.aipanel.app.ui.Theme;
import io.aipanel.app.ui.settings.components.AnimatedSettingsButton;
import io.aipanel.app.ui.settings.components.AnimatedToggleSwitch;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class SettingsPanel extends JPanel {

    @Setter
    private Consumer<Boolean> onRememberLastAiChanged;
    @Setter
    private Runnable onClearCookies;
    @Setter
    private Consumer<Boolean> onZoomEnabledChanged;
    @Setter
    private Runnable onImportCookies;

    private final AnimatedToggleSwitch rememberToggle;
    private final AnimatedToggleSwitch zoomToggle;

    public SettingsPanel(AppPreferences appPreferences) {
        setOpaque(true);
        setBackground(Theme.BG_BAR);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        addSection("General");

        rememberToggle = new AnimatedToggleSwitch(appPreferences.isRememberLastAi());
        rememberToggle.setOnChange(val -> {
            if (onRememberLastAiChanged != null) onRememberLastAiChanged.accept(val);
        });
        addSettingRow("Remember last AI", rememberToggle);

        add(Box.createVerticalStrut(16));
        addSection("Browser");

        zoomToggle = new AnimatedToggleSwitch(appPreferences.isZoomEnabled());
        zoomToggle.setOnChange(val -> {
            if (onZoomEnabledChanged != null) onZoomEnabledChanged.accept(val);
        });
        addSettingRow("Zoom enabled", zoomToggle);

        add(Box.createVerticalStrut(12));

        var buttonsRow = new JPanel();
        buttonsRow.setOpaque(false);
        buttonsRow.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        var clearCookiesBtn = new AnimatedSettingsButton("Clear cookies", () -> {
            if (onClearCookies != null) onClearCookies.run();
        });
        buttonsRow.add(clearCookiesBtn);

        buttonsRow.add(Box.createHorizontalStrut(8));

        var importCookiesBtn = new AnimatedSettingsButton("Import cookies", () -> {
            if (onImportCookies != null) onImportCookies.run();
        });
        buttonsRow.add(importCookiesBtn);

        add(buttonsRow);
    }

    private void addSection(String title) {
        var label = new JLabel(title.toUpperCase());
        label.setFont(Theme.FONT_SETTINGS_SECTION);
        label.setForeground(Theme.TEXT_TERTIARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(label);
        add(Box.createVerticalStrut(10));
    }

    private void addSettingRow(String labelText, JComponent control) {
        var row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        var label = new JLabel(labelText);
        label.setFont(Theme.FONT_SETTINGS);
        label.setForeground(Theme.TEXT_PRIMARY);

        row.add(label);
        row.add(Box.createHorizontalGlue());
        row.add(control);

        add(row);
        add(Box.createVerticalStrut(8));
    }
}