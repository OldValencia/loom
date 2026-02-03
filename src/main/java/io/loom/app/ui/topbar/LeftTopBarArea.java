package io.loom.app.ui.topbar;

import io.loom.app.config.AiConfiguration;
import io.loom.app.config.AppPreferences;
import io.loom.app.ui.CefWebView;
import io.loom.app.ui.topbar.components.AiDock;
import lombok.RequiredArgsConstructor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

@RequiredArgsConstructor
class LeftTopBarArea {

    private final AiConfiguration aiConfiguration;
    private final CefWebView cefWebView;
    private final AppPreferences appPreferences;

    JPanel buildLeftArea() {
        var wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(new AiDock(aiConfiguration.getConfigurations(), cefWebView, appPreferences));
        wrapper.setBorder(new EmptyBorder(0, 15, 0, 0));
        return wrapper;
    }
}
