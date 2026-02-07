package io.loom.app.ui.topbar;

import io.loom.app.ui.CefWebView;
import io.loom.app.ui.Theme;
import io.loom.app.ui.topbar.components.AnimatedIconButton;
import io.loom.app.ui.topbar.components.ZoomButton;

import javax.swing.*;
import java.awt.*;

class RightTopBarArea extends Box {

    public RightTopBarArea(CefWebView cefWebView, Runnable onSettingsToggle, Runnable onCloseWindow) {
        super(BoxLayout.X_AXIS);

        var wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        var zoomButton = new ZoomButton(cefWebView::resetZoom);
        cefWebView.setZoomCallback(zoomButton::updateZoomDisplay);
        gbc.insets = new Insets(0, 0, 0, 15);
        wrapper.add(zoomButton, gbc);

        gbc.gridx++;
        gbc.insets = new Insets(0, 0, 0, 6);
        wrapper.add(new AnimatedIconButton("⚙", Theme.BTN_HOVER_SETTINGS, onSettingsToggle), gbc);

        gbc.gridx++;
        gbc.insets = new Insets(0, 0, 0, 0);
        wrapper.add(new AnimatedIconButton("✕", Theme.BTN_HOVER_CLOSE, onCloseWindow), gbc);

        this.add(wrapper);
        this.add(Box.createHorizontalStrut(10));
    }
}
