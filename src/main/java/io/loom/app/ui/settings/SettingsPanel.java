package io.loom.app.ui.settings;

import io.loom.app.config.AppPreferences;
import io.loom.app.ui.Theme;
import io.loom.app.ui.settings.components.AnimatedSettingsButton;
import io.loom.app.ui.settings.components.AnimatedToggleSwitch;
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

    public SettingsPanel(AppPreferences appPreferences) {
        setOpaque(true);
        setBackground(Theme.BG_BAR);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        buildSection("General", appPreferences.isRememberLastAi(), onRememberLastAiChanged, "Remember last used AI");
        add(Box.createVerticalStrut(16));
        buildSection("Browser", appPreferences.isZoomEnabled(), onZoomEnabledChanged, "Zoom enabled");

        add(Box.createVerticalStrut(12));

        var buttonsRow = new JPanel();
        buttonsRow.setOpaque(false);
        buttonsRow.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        var clearCookiesBtn = new AnimatedSettingsButton("Clear cookies", () -> {
            if (onClearCookies != null) onClearCookies.run();
        });
        buttonsRow.add(clearCookiesBtn);

        add(buttonsRow);
    }

    private void buildSection(String General, boolean appPreferences, Consumer<Boolean> onChanged, String rowString) {
        addSection(General);

        var rememberToggle = new AnimatedToggleSwitch(appPreferences);
        rememberToggle.setOnChange(val -> {
            if (onChanged != null) onChanged.accept(val);
        });
        addSettingRow(rowString, rememberToggle);
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
