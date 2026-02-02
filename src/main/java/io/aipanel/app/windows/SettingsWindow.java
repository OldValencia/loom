package io.aipanel.app.windows;

import io.aipanel.app.ui.CefWebView;
import io.aipanel.app.ui.Theme;
import lombok.RequiredArgsConstructor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;

@RequiredArgsConstructor
public class SettingsWindow {

    private final JFrame parentFrame;
    private final CefWebView cefWebView;

    private JDialog window;
    private float animProgress = 0f;
    private float targetProgress = 0f;
    private Timer animTimer;

    private static final int WIDTH = 340;
    private static final int HEIGHT = 500;

    private void createWindow() {
        window = new JDialog(parentFrame, "Settings", false); // modeless
        window.setUndecorated(true);
        window.setSize(WIDTH, HEIGHT);
        window.setBackground(new Color(0, 0, 0, 0));

        // Создаем панель с содержимым
//        SettingsPanel settingsPanel = new SettingsPanel(cefWebView, window);

        // Оборачиваем в контейнер, который рисует тень и скругление
        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Полупрозрачный фон (для плавного появления)
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, animProgress));

                g2.setColor(Theme.BG_POPUP);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));

                // Border
                g2.setColor(Theme.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 16, 16));
            }
        };
        root.setOpaque(false);
//        root.add(settingsPanel, BorderLayout.CENTER);
        window.setContentPane(root);

        // Animation logic
        animTimer = new Timer(10, e -> {
            float diff = targetProgress - animProgress;
            if (Math.abs(diff) < 0.01f) {
                animProgress = targetProgress;
                animTimer.stop();
                if (targetProgress == 0f) window.setVisible(false);
            } else {
                animProgress += diff * 0.2f;
            }
            window.repaint();

            // Sync location with parent (slide form right)
            int targetX = parentFrame.getX() + (parentFrame.getWidth() - WIDTH) / 2;
            int startX = parentFrame.getX() + parentFrame.getWidth();
            // Optional: Slide effect
            int currentX = targetX;
            window.setLocation(currentX, parentFrame.getY() + (parentFrame.getHeight() - HEIGHT) / 2);
        });

        // Close on lost focus
        window.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                close();
            }
        });
    }

    public boolean isOpen() {
        return targetProgress > 0.5f;
    }

    public void open() {
        if (window == null) {
            createWindow();
        }
        targetProgress = 1f;
        window.setVisible(true);

        // Теперь эти методы работают
        cefWebView.showDim();

        animTimer.start();
    }

    public void close() {
        if (window == null) return;
        targetProgress = 0f;

        // И этот тоже
        cefWebView.hideDim();

        animTimer.start();
    }
}