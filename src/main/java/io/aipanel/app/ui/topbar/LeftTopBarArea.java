package io.aipanel.app.ui.topbar;

import io.aipanel.app.config.AiConfiguration;
import io.aipanel.app.ui.CefWebView;
import io.aipanel.app.ui.Theme;
import lombok.RequiredArgsConstructor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
class LeftTopBarArea {

    private final AiConfiguration aiConfiguration;
    private final CefWebView cefWebView;

    JPanel buildLeftArea() {
        var wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 12));
        wrapper.setOpaque(false);
        wrapper.add(new AiSelector(aiConfiguration.getConfigurations(), cefWebView));
        return wrapper;
    }

    static class AiSelector extends JPanel {

        private static final int  ROW_H       = 36;
        private static final int  ICON_SIZE   = 23;
        private static final int  PAD_X       = 12;
        private static final int  MAX_VISIBLE = 7;

        private final List<AiConfiguration.AiConfig> configs;
        private final CefWebView                     cefWebView;

        private int     selectedIndex = 0;
        private JWindow popup         = null;
        private boolean popupVisible  = false;
        private AWTEventListener outsideClickListener;

        private static final Map<String, Image> ICON_CACHE = new ConcurrentHashMap<>();
        private final int triggerWidth;

        AiSelector(List<AiConfiguration.AiConfig> configs, CefWebView cefWebView) {
            this.configs    = configs;
            this.cefWebView = cefWebView;

            var fm = new Canvas().getFontMetrics(Theme.FONT_SELECTOR);
            int maxW = 0;
            for (var c : configs) maxW = Math.max(maxW, fm.stringWidth(c.name()));
            this.triggerWidth = ICON_SIZE + PAD_X * 3 + maxW + 26;

            setOpaque(false);
            setPreferredSize(new Dimension(triggerWidth, 30));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            for (var cfg : configs) preloadIcon(cfg);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { togglePopup(); }
            });
        }

        private void preloadIcon(AiConfiguration.AiConfig cfg) {
            if (cfg.icon() == null) return;
            ICON_CACHE.computeIfAbsent(cfg.icon(), key -> {
                var url = AiSelector.class.getResource("/icons/" + key);
                if (url == null) return null;
                return new ImageIcon(url).getImage()
                        .getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
            });
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            var g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = triggerWidth, h = getHeight();

            g.setColor(Theme.BG_POPUP);
            g.fill(new RoundRectangle2D.Float(0, 0, w, h, 8, 8));

            g.setColor(Theme.BORDER);
            g.setStroke(new BasicStroke(1f));
            g.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, 8, 8));

            var cfg  = configs.get(selectedIndex);
            var icon = ICON_CACHE.get(cfg.icon());
            if (icon != null)
                g.drawImage(icon, PAD_X, (h - ICON_SIZE) / 2, ICON_SIZE, ICON_SIZE, null);

            g.setColor(Theme.TEXT_PRIMARY);
            g.setFont(Theme.FONT_SELECTOR);
            g.drawString(cfg.name(), PAD_X + ICON_SIZE + PAD_X, h / 2 + 4);

            g.setColor(Theme.TEXT_SECONDARY);
            g.setFont(Theme.FONT_SELECTOR);
            g.drawString("▼", w - PAD_X - 4, h / 2 + 4);
        }

        private void togglePopup() {
            if (popupVisible) closePopup();
            else              openPopup();
        }

        private void openPopup() {
            popup = new JWindow();
            popup.setAlwaysOnTop(true);
            popup.setContentPane(new PopupPanel());
            popup.pack();

            var loc = getLocationOnScreen();
            popup.setLocation(loc.x, loc.y + getHeight() + 4);
            popup.setVisible(true);
            popupVisible = true;

            outsideClickListener = event -> {
                if (event instanceof MouseEvent me && me.getID() == MouseEvent.MOUSE_PRESSED) {
                    var clickPt = me.getLocationOnScreen();
                    if (popup != null && popup.isVisible() && popup.getBounds().contains(clickPt)) return;
                    if (new Rectangle(getLocationOnScreen(), getSize()).contains(clickPt)) return;
                    closePopup();
                }
            };
            Toolkit.getDefaultToolkit().addAWTEventListener(outsideClickListener, AWTEvent.MOUSE_EVENT_MASK);
        }

        private void closePopup() {
            if (popup != null) { popup.setVisible(false); popup.dispose(); popup = null; }
            popupVisible = false;
            if (outsideClickListener != null) {
                Toolkit.getDefaultToolkit().removeAWTEventListener(outsideClickListener);
                outsideClickListener = null;
            }
            repaint();
        }

        class PopupPanel extends JPanel {

            PopupPanel() {
                setBackground(Theme.BG_POPUP);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 1, 1, 1, Theme.BORDER),
                        new EmptyBorder(4, 0, 4, 0)
                ));

                int visCount = Math.min(configs.size(), MAX_VISIBLE);
                setPreferredSize(new Dimension(triggerWidth, visCount * ROW_H));

                if (configs.size() > MAX_VISIBLE) {
                    var list = new JPanel();
                    list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
                    list.setBackground(Theme.BG_POPUP);
                    for (int i = 0; i < configs.size(); i++) list.add(new DropdownRow(i));

                    var scroll = new JScrollPane(list,
                            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                    scroll.setBorder(BorderFactory.createEmptyBorder());
                    scroll.setBackground(Theme.BG_POPUP);
                    scroll.getViewport().setBackground(Theme.BG_POPUP);
                    scroll.getVerticalScrollBar().setUnitIncrement(ROW_H);
                    scroll.getVerticalScrollBar().setUI(new ThinScrollBarUI());

                    setLayout(new BorderLayout());
                    add(scroll, BorderLayout.CENTER);
                } else {
                    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                    for (int i = 0; i < configs.size(); i++) add(new DropdownRow(i));
                }
            }
        }

        class DropdownRow extends JPanel {

            private boolean hovered = false;
            private final int index;

            DropdownRow(int index) {
                this.index = index;
                setOpaque(false);
                setPreferredSize(new Dimension(0, ROW_H));
                setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_H));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                    @Override public void mouseClicked(MouseEvent e) {
                        selectedIndex = index;
                        cefWebView.loadUrl(configs.get(index).url());
                        closePopup();
                        AiSelector.this.repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g0) {
                super.paintComponent(g0);
                var g = (Graphics2D) g0;
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                if (hovered) {
                    g.setColor(Theme.BG_HOVER);
                    g.fill(new RoundRectangle2D.Float(4, 2, w - 8, h - 4, 6, 6));
                }

                if (index == selectedIndex) {
                    g.setColor(Theme.ACCENT);
                    g.fill(new RoundRectangle2D.Float(0, h / 2f - 7, 3, 14, 3, 3));
                }

                var cfg  = configs.get(index);
                var icon = ICON_CACHE.get(cfg.icon());
                if (icon != null)
                    g.drawImage(icon, PAD_X + 5, (h - ICON_SIZE) / 2, ICON_SIZE, ICON_SIZE, null);

                g.setColor(index == selectedIndex ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
                g.setFont(Theme.FONT_SELECTOR);
                g.drawString(cfg.name(), PAD_X + ICON_SIZE + PAD_X + 5, h / 2 + 4);
            }
        }
    }

    static class ThinScrollBarUI extends BasicScrollBarUI {

        @Override
        protected void setThumbBounds(int x, int y, int width, int height) {
            super.setThumbBounds(x, y, 6, height);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            var btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            return btn;
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            var btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            return btn;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(Theme.BG_POPUP);
            g.fillRect(r.x, r.y, r.width, r.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            var g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.BORDER_LIGHT);
            g2.fill(new RoundRectangle2D.Float(r.x + 1, r.y + 2, r.width - 2, r.height - 4, 4, 4));
        }
    }
}
