package io.loom.app.ui.settings.components;

import javax.swing.*;
import java.awt.*;

public class ClearCookiesButton extends JPanel {
    public ClearCookiesButton(Runnable onClearCookies) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.setOpaque(false);
        this.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));

        var clearCookiesBtn = new AnimatedSettingsButton("Clear cookies", () -> {
            if (onClearCookies != null) onClearCookies.run();
        });

        this.add(clearCookiesBtn);
    }
}
