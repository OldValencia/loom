package io.loom.app.ui.settings.components;

import io.loom.app.ui.Theme;
import io.loom.app.utils.GlobalHotkeyManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class HotkeySection extends JPanel {
    public HotkeySection(List<Integer> hotkeyToStartApplication, GlobalHotkeyManager hotkeyManager) {
        super();
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setOpaque(false);
        this.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));

        this.add(buildTitle());
        this.add(Box.createHorizontalGlue());
        var hotkeyRecordButton = buildHotkeyRecordButton(hotkeyToStartApplication, hotkeyManager);
        this.add(hotkeyRecordButton);
        this.add(Box.createHorizontalStrut(8));
        this.add(buildResetHotkeyButton(hotkeyManager, hotkeyRecordButton));
    }

    private JLabel buildTitle() {
        var title = new JLabel("Toggle Window Shortcut");
        title.setFont(Theme.FONT_SETTINGS);
        title.setForeground(Theme.TEXT_PRIMARY);
        return title;
    }

    private AnimatedSettingsButton buildHotkeyRecordButton(List<Integer> hotkeyToStartApplication, GlobalHotkeyManager hotkeyManager) {
        var currentHotkey = GlobalHotkeyManager.getHotkeyText(hotkeyToStartApplication);
        var initialText = currentHotkey.isEmpty() ? "Click to Record" : currentHotkey;
        var btnRef = new AtomicReference<AnimatedSettingsButton>();

        Runnable action = () -> {
            var button = btnRef.get();
            if (button != null) {
                button.setText("Press keys... (Esc to cancel)");
                hotkeyManager.startRecording(() -> {
                    var newHotkey = GlobalHotkeyManager.getHotkeyText(hotkeyToStartApplication);
                    button.setText(newHotkey);
                });
            }
        };

        var hotkeyRecordBtn = new AnimatedSettingsButton(initialText, action);
        btnRef.set(hotkeyRecordBtn);
        return hotkeyRecordBtn;
    }

    private ColorfulButton buildResetHotkeyButton(GlobalHotkeyManager hotkeyManager, AnimatedSettingsButton hotkeyRecordButton) {
        var resetColor = new Color(255, 94, 91);
        return new ColorfulButton("✖", resetColor, () -> {
            if (hotkeyManager != null) {
                hotkeyManager.clearHotkey();
                hotkeyRecordButton.setText("None");
            }
        });
    }
}
