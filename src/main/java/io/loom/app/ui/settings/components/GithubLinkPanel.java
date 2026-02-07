package io.loom.app.ui.settings.components;

import io.loom.app.ui.Theme;
import io.loom.app.ui.settings.utils.UrlUtils;
import io.loom.app.utils.SystemUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GithubLinkPanel extends JPanel {

    private static final String GITHUB_URL = "https://github.com/oldvalencia/loom";
    private static final String VERSION_TEMPLATE = "Loom application on Github (v %s)";

    public GithubLinkPanel() {
        super(new FlowLayout(FlowLayout.CENTER));
        setOpaque(false);
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setMaximumSize(new Dimension(Short.MAX_VALUE, 30));

        add(buildGithubLabel());
    }

    private JLabel buildGithubLabel() {
        var text = VERSION_TEMPLATE.formatted(SystemUtils.VERSION);
        var label = new JLabel(text);

        label.setFont(Theme.FONT_SETTINGS.deriveFont(11f));
        label.setForeground(Theme.TEXT_TERTIARY);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                UrlUtils.openLink(GITHUB_URL);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                label.setForeground(Theme.TEXT_SECONDARY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                label.setForeground(Theme.TEXT_TERTIARY);
            }
        });

        return label;
    }
}