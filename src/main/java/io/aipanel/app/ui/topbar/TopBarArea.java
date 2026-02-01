package io.aipanel.app.ui.topbar;

import io.aipanel.app.config.AiConfiguration;
import io.aipanel.app.ui.CefWebView;
import io.aipanel.app.ui.Theme;
import lombok.RequiredArgsConstructor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

@RequiredArgsConstructor
public class TopBarArea {

    private final AiConfiguration aiConfiguration;
    private final CefWebView cefWebView;
    private final JFrame frame;

    private Point initialClick;

    public JPanel createTopBar() {
        var topBar = new MicaPanel();
        topBar.setPreferredSize(new Dimension(frame.getWidth(), 48));
        topBar.setLayout(new BorderLayout());

        var leftTopBarArea = new LeftTopBarArea(aiConfiguration, cefWebView);
        topBar.add(leftTopBarArea.buildLeftArea(), BorderLayout.WEST);

        var rightTopBarArea = new RightTopBarArea(cefWebView);
        topBar.add(rightTopBarArea.buildRightArea(), BorderLayout.EAST);

        setupDragging(topBar);
        return topBar;
    }


    private void setupDragging(JPanel panel) {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });
        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - initialClick.x;
                int dy = e.getY() - initialClick.y;
                var loc = frame.getLocation();
                frame.setLocation(loc.x + dx, loc.y + dy);
            }
        });
    }

    static class MicaPanel extends JPanel {

        MicaPanel() { setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g0) {
            var g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            // полупрозрачный фон
            g.setColor(Theme.BG_BAR);
            g.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));

            // gradient для глубины
            g.setPaint(new GradientPaint(
                    0, 0, new Color(40, 40, 48, 55),
                    0, h, new Color(28, 28, 34, 0)
            ));
            g.fill(new RoundRectangle2D.Float(0, 0, w, h, 14, 14));

            // border снизу
            g.setColor(Theme.BORDER);
            g.setStroke(new BasicStroke(1f));
            g.drawLine(0, h - 1, w, h - 1);
        }
    }
}
