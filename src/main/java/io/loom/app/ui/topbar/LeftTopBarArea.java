package io.loom.app.ui.topbar;

import io.loom.app.config.AiConfiguration;
import io.loom.app.config.AppPreferences;
import io.loom.app.ui.CefWebView;
import io.loom.app.ui.topbar.components.AiDock;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

class LeftTopBarArea extends JPanel {

    public LeftTopBarArea(AiConfiguration aiConfiguration, CefWebView cefWebView, AppPreferences appPreferences) {
        super(new GridBagLayout());

        this.setOpaque(false);
        this.add(new AiDock(aiConfiguration.getConfigurations(), cefWebView, appPreferences));
        this.setBorder(new EmptyBorder(0, 15, 0, 0));
    }
}
