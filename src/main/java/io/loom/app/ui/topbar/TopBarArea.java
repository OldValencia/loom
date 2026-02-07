package io.loom.app.ui.topbar;

import io.loom.app.config.AiConfiguration;
import io.loom.app.config.AppPreferences;
import io.loom.app.ui.CefWebView;
import io.loom.app.ui.topbar.components.GradientPanel;
import io.loom.app.windows.SettingsWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TopBarArea extends GradientPanel {

    private final JFrame frame;
    private final SettingsWindow settingsWindow;

    private Point initialClick;

    public TopBarArea(AiConfiguration aiConfiguration, CefWebView cefWebView, JFrame frame, SettingsWindow settingsWindow, AppPreferences appPreferences, Runnable onSettingsToggle, Runnable onCloseWindow) {
        super();

        this.frame = frame;
        this.settingsWindow = settingsWindow;

        this.setPreferredSize(new Dimension(frame.getWidth(), 48));
        this.setLayout(new BorderLayout());

        this.add(new LeftTopBarArea(aiConfiguration, cefWebView, appPreferences), BorderLayout.WEST);
        this.add(new RightTopBarArea(cefWebView, onSettingsToggle, onCloseWindow), BorderLayout.EAST);

        setupDragging(this);

        if (!aiConfiguration.getConfigurations().isEmpty()) {
            this.updateAccentColor(Color.decode(aiConfiguration.getConfigurations().getFirst().color()));
        }
    }

    private void setupDragging(JPanel panel) {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();

                if (settingsWindow != null && settingsWindow.isOpen()) {
                    settingsWindow.close();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                initialClick = null;
            }
        });

        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (initialClick == null) {
                    return;
                }

                int xOnScreen = e.getLocationOnScreen().x;
                int yOnScreen = e.getLocationOnScreen().y;
                int newX = xOnScreen - initialClick.x;
                int newY = yOnScreen - initialClick.y;

                frame.setLocation(newX, newY);
            }
        });
    }
}
