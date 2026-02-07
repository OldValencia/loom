package io.loom.app.ui.settings.components;

import io.loom.app.ui.settings.utils.UrlUtils;

import javax.swing.*;
import java.awt.*;

public class DonationSection extends JPanel {
    public DonationSection() {
        super(new FlowLayout(FlowLayout.CENTER, 10, 0));
        this.setOpaque(false);
        this.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));

        var coffeeColor = new Color(255, 200, 0);
        this.add(new ColorfulButton("☕ Buy me a coffee", coffeeColor,
                () -> UrlUtils.openLink("https://buymeacoffee.com/oldvalencia")));

        var kofiColor = new Color(255, 94, 91);
        this.add(new ColorfulButton("❤️ Ko-Fi", kofiColor,
                () -> UrlUtils.openLink("https://ko-fi.com/oldvalencia")));
    }
}
