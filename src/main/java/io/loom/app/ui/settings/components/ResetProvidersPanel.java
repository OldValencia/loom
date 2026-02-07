package io.loom.app.ui.settings.components;

import io.loom.app.config.AiConfiguration;

import javax.swing.*;
import java.awt.*;

public class ResetProvidersPanel extends JPanel {

    private static final Color RESET_BUTTON_COLOR = new Color(255, 94, 91);
    private final AiConfiguration aiConfiguration;
    private final Runnable onProvidersChanged;

    public ResetProvidersPanel(AiConfiguration aiConfiguration, Runnable onProvidersChanged) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));

        this.aiConfiguration = aiConfiguration;
        this.onProvidersChanged = onProvidersChanged;

        initUi();
    }

    private void initUi() {
        this.setOpaque(false);
        this.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));

        var resetButton = createResetButton();
        this.add(resetButton);
    }

    private ColorfulButton createResetButton() {
        return new ColorfulButton(
                "Reset Providers to Default",
                RESET_BUTTON_COLOR,
                this::handleResetAction
        );
    }

    private void handleResetAction() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure? This will delete all custom providers and icons.",
                "Reset Configuration",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            aiConfiguration.resetToDefaults();
            if (onProvidersChanged != null) {
                onProvidersChanged.run();
            }
        }
    }
}