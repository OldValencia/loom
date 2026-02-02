package io.aipanel.app.ui.dialogs;

import io.aipanel.app.ui.Theme;
import io.aipanel.app.utils.BrowserCookieImporter;
import io.aipanel.app.utils.LogSetup;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Paths;

public class CookieImportDialog extends JDialog {

    private final JFrame owner;

    public CookieImportDialog(JFrame owner) {
        super(owner, "Import Cookies", true);
        this.owner = owner;

        setUndecorated(true);
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));

        initUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        var contentPanel = new JPanel();
        contentPanel.setBackground(Theme.BG_BAR);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        var titleLabel = new JLabel("Import Browser Cookies");
        titleLabel.setFont(new Font(Theme.FONT_NAME, Font.BOLD, 16));
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);

        contentPanel.add(Box.createVerticalStrut(8));

        var descLabel = new JLabel("Select browser to import cookies from:");
        descLabel.setFont(Theme.FONT_SETTINGS);
        descLabel.setForeground(Theme.TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(descLabel);

        contentPanel.add(Box.createVerticalStrut(16));

        var profiles = BrowserCookieImporter.detectBrowsers();

        if (profiles.isEmpty()) {
            var noProfilesLabel = new JLabel("No browser profiles detected");
            noProfilesLabel.setFont(Theme.FONT_SETTINGS);
            noProfilesLabel.setForeground(Theme.TEXT_TERTIARY);
            noProfilesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(noProfilesLabel);
        } else {
            for (var profile : profiles) {
                var btn = createBrowserButton(profile);
                btn.setAlignmentX(Component.LEFT_ALIGNMENT);
                contentPanel.add(btn);
                contentPanel.add(Box.createVerticalStrut(8));
            }
        }

        contentPanel.add(Box.createVerticalStrut(8));

        var cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(Theme.FONT_SETTINGS);
        cancelBtn.setBackground(Theme.BG_POPUP);
        cancelBtn.setForeground(Theme.TEXT_PRIMARY);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        cancelBtn.addActionListener(e -> dispose());
        contentPanel.add(cancelBtn);

        setContentPane(contentPanel);
    }

    private JButton createBrowserButton(BrowserCookieImporter.BrowserProfile profile) {
        var btn = new JButton("Import from " + profile.browser() + " (" + profile.profileName() + ")");
        btn.setFont(Theme.FONT_SETTINGS);
        btn.setBackground(Theme.BG_POPUP);
        btn.setForeground(Theme.TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(350, 36));
        btn.setMaximumSize(new Dimension(350, 36));

        btn.addActionListener(e -> {
            var targetDir = Paths.get(LogSetup.APP_DIR, "imported_cookies");
            boolean success = BrowserCookieImporter.importCookies(profile, targetDir);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Cookies imported successfully from " + profile.browser() + "!\n" +
                                "Restart the application to use imported cookies.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to import cookies from " + profile.browser() + ".\n" +
                                "Make sure the browser is closed.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        return btn;
    }
}