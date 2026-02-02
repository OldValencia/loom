package io.aipanel.app.ui.topbar.components;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import io.aipanel.app.config.AiConfiguration;
import io.aipanel.app.config.AppPreferences;
import io.aipanel.app.ui.CefWebView;
import io.aipanel.app.ui.Theme;
import lombok.Getter;
import lombok.Setter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AiDock extends JPanel {

    private static final int ICON_SIZE = 24;
    private static final int ITEM_HEIGHT = 32;
    private static final int PAD = 12;
    private static final int GAP = 8;
    private static final int ITEM_MARGIN = 6;
    private static final int DRAG_THRESHOLD = 10;

    private final List<DockItem> dockItems = new ArrayList<>();
    private final CefWebView cefWebView;
    private final AppPreferences appPreferences;
    private final Timer animationTimer;

    private static final Map<String, Image> ICON_CACHE = new ConcurrentHashMap<>();
    private int selectedIndex = 0;

    private boolean isDragging = false;
    private int dragStartIndex = -1;
    private int pressX = -1;
    private int pressY = -1;

    public AiDock(List<AiConfiguration.AiConfig> configs, CefWebView cefWebView, AppPreferences appPreferences) {
        this.cefWebView = cefWebView;
        this.appPreferences = appPreferences;

        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String lastUrl = appPreferences.getLastUrl();
        int initialIndex = 0;

        if (lastUrl != null) {
            for (int i = 0; i < configs.size(); i++) {
                if (configs.get(i).url().equals(lastUrl)) {
                    initialIndex = i;
                    break;
                }
            }
        }
        this.selectedIndex = initialIndex;

        for (int i = 0; i < configs.size(); i++) {
            var item = new DockItem(configs.get(i), i);
            if (i == initialIndex) {
                item.setSelected(true);
            }
            dockItems.add(item);
            preloadIcon(configs.get(i));
        }

        animationTimer = new Timer(15, e -> animate());
        setupMouseListeners();

        calculateTargets(false);
        for (var item : dockItems) {
            item.currentWidth = item.targetWidth;
        }
        revalidateWidth();

        SwingUtilities.invokeLater(() -> {
            updateTopBarColor();
            // Устанавливаем конфиг для рестарта при старте
            if (!dockItems.isEmpty()) {
                cefWebView.setCurrentConfig(dockItems.get(selectedIndex).config);
            }
        });
    }

    private void setupMouseListeners() {
        var ma = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isDragging) animationTimer.start();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!isDragging) {
                    for (var item : dockItems) item.setHovered(false);
                    animationTimer.start();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    pressX = e.getX();
                    pressY = e.getY();
                    dragStartIndex = getItemIndexAt(pressX);
                    isDragging = false;
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) return;

                if (!isDragging) {
                    if (Math.abs(e.getX() - pressX) > DRAG_THRESHOLD || Math.abs(e.getY() - pressY) > DRAG_THRESHOLD) {
                        isDragging = true;
                    }
                }

                if (isDragging && dragStartIndex != -1) {
                    handleDrag(e.getX());
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) return;

                if (isDragging) {
                    isDragging = false;
                    dragStartIndex = -1;
                    revalidateWidth();
                    repaint();
                } else {
                    int index = getItemIndexAt(e.getX());
                    if (index != -1) {
                        selectItem(index);
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (!isDragging) handleMouseMove(e.getX());
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    // === ИСПРАВЛЕННЫЙ МЕТОД: Проверяем элементы строго по порядку ===
    private int getItemIndexAt(int x) {
        float currentX = 0;

        for (int i = 0; i < dockItems.size(); i++) {
            DockItem item = dockItems.get(i);

            // Если элемент скрыт (width=0), пропускаем его
            if (item.currentWidth < 1) continue;

            if (x >= currentX && x <= currentX + item.currentWidth) {
                return i;
            }
            currentX += item.currentWidth + ITEM_MARGIN;
        }
        return -1;
    }

    private void handleDrag(int x) {
        int targetIndex = getItemIndexAt(x);

        // Переставляем, если мышка оказалась над другим элементом
        if (targetIndex != -1 && targetIndex != dragStartIndex) {
            Collections.swap(dockItems, dragStartIndex, targetIndex);

            // Обновляем selectedIndex, чтобы он указывал на тот же логический элемент
            if (selectedIndex == dragStartIndex) selectedIndex = targetIndex;
            else if (selectedIndex == targetIndex) selectedIndex = dragStartIndex;

            dragStartIndex = targetIndex;

            calculateTargets(true);
            revalidateWidth();
        }
    }

    private void calculateTargets(boolean isDockHovered) {
        for (var item : dockItems) {
            float targetW;
            // Считаем hovered, если мышь над ним ИЛИ если мы его тащим (чтобы он не схлопывался под пальцем)
            boolean isItemHovered = item.isHovered() || (isDragging && dockItems.indexOf(item) == dragStartIndex);

            if (item.isSelected()) {
                targetW = PAD + ICON_SIZE + GAP + getTextWidth(item.config.name()) + PAD;
            } else {
                if (isDockHovered || isDragging) {
                    if (isItemHovered) {
                        targetW = PAD + ICON_SIZE + GAP + getTextWidth(item.config.name()) + PAD;
                    } else {
                        targetW = PAD + ICON_SIZE + PAD;
                    }
                } else {
                    targetW = 0f;
                }
            }
            item.setTargetWidth(targetW);
        }
    }

    private void animate() {
        boolean needsRepaint = false;
        boolean allDone = true;
        boolean isDockHovered = (getMousePosition() != null) || isDragging;

        calculateTargets(isDockHovered);

        for (var item : dockItems) {
            float diff = item.targetWidth - item.currentWidth;
            if (Math.abs(diff) > 0.5f) {
                item.currentWidth += diff * 0.2f;
                needsRepaint = true;
                allDone = false;
            } else {
                item.currentWidth = item.targetWidth;
            }
        }

        if (needsRepaint) {
            revalidateWidth();
            repaintParents();
        } else if (allDone && !isDockHovered) {
            animationTimer.stop();
        }
    }

    private void revalidateWidth() {
        int totalW = 0;
        for (var item : dockItems) {
            totalW += (int) item.currentWidth;
            if (item.currentWidth > 1) totalW += ITEM_MARGIN;
        }
        setPreferredSize(new Dimension(totalW, 48));
        revalidate();
    }

    private void repaintParents() {
        var topBar = SwingUtilities.getAncestorOfClass(GradientPanel.class, this);
        if (topBar != null) topBar.repaint(); else repaint();
    }

    private void handleMouseMove(int x) {
        int hoverIndex = getItemIndexAt(x);
        boolean changed = false;
        for (int i = 0; i < dockItems.size(); i++) {
            boolean shouldHover = (i == hoverIndex);
            if (dockItems.get(i).isHovered() != shouldHover) {
                dockItems.get(i).setHovered(shouldHover);
                changed = true;
            }
        }
        if (changed) animationTimer.start();
    }

    private void selectItem(int index) {
        if (index == selectedIndex) return;

        dockItems.get(selectedIndex).setSelected(false);
        selectedIndex = index;
        DockItem newItem = dockItems.get(index);
        newItem.setSelected(true);

        // Обновляем браузер
        cefWebView.loadUrl(newItem.config.url());
        cefWebView.setCurrentConfig(newItem.config);

        if (appPreferences != null) {
            appPreferences.setLastUrl(newItem.config.url());
        }

        updateTopBarColor();
        animationTimer.start();
    }

    private void updateTopBarColor() {
        var topBarComp = SwingUtilities.getAncestorOfClass(GradientPanel.class, this);
        if (topBarComp instanceof GradientPanel topBar) {
            Color accent = Theme.ACCENT;
            try {
                if (dockItems.get(selectedIndex).config.color() != null)
                    accent = Color.decode(dockItems.get(selectedIndex).config.color());
            } catch (Exception ignored) {}
            topBar.setAccentColor(accent);
            topBar.repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        var g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int y = (getHeight() - ITEM_HEIGHT) / 2;
        float currentX = 0;

        // Рисуем СТРОГО по порядку списка
        for (DockItem item : dockItems) {
            if (item.currentWidth > 1.0f) {
                drawDockItem(g, item, (int) currentX, y);
                currentX += item.currentWidth + ITEM_MARGIN;
            }
        }
    }

    private void drawDockItem(Graphics2D g, DockItem item, int x, int y) {
        int w = (int) item.currentWidth;
        if (w <= 0) return;

        Shape shape = new RoundRectangle2D.Float(x, y, w, ITEM_HEIGHT, ITEM_HEIGHT, ITEM_HEIGHT);

        if (item.isSelected()) {
            var bg = Theme.ACCENT;
            try {
                if (item.config.color() != null) bg = Color.decode(item.config.color());
            } catch (Exception ignored) {}
            g.setColor(bg);
            g.fill(shape);
        } else {
            g.setColor(item.isHovered() ? Theme.BG_HOVER : Theme.BG_POPUP);
            g.fill(shape);
            g.setColor(Theme.BORDER);
            g.setStroke(new BasicStroke(1f));
            g.draw(shape);
        }

        var icon = ICON_CACHE.get(item.config.icon());
        if (icon != null) {
            int iconY = y + (ITEM_HEIGHT - ICON_SIZE) / 2;
            g.drawImage(icon, x + PAD, iconY, ICON_SIZE, ICON_SIZE, null);
        }

        if (w > PAD + ICON_SIZE + PAD) {
            g.setFont(Theme.FONT_SELECTOR);
            g.setColor(Theme.TEXT_PRIMARY);
            var fm = g.getFontMetrics();
            int textX = x + PAD + ICON_SIZE + GAP;
            int textY = y + (ITEM_HEIGHT + fm.getAscent() - fm.getDescent()) / 2;
            Shape oldClip = g.getClip();
            g.setClip(shape);
            g.drawString(item.config.name(), textX, textY);
            g.setClip(oldClip);
        }
    }

    private int getTextWidth(String text) {
        return new Canvas().getFontMetrics(Theme.FONT_SELECTOR).stringWidth(text);
    }

    private void preloadIcon(AiConfiguration.AiConfig cfg) {
        if (cfg.icon() == null) return;
        ICON_CACHE.computeIfAbsent(cfg.icon(), key -> {
            try {
                var url = AiDock.class.getResource("/icons/" + key);
                if (url == null) return null;
                if (key.toLowerCase().endsWith(".svg")) {
                    var icon = new FlatSVGIcon(url);
                    var scaledIcon = icon.derive(ICON_SIZE, ICON_SIZE);
                    var img = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
                    var g = img.createGraphics();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    scaledIcon.paintIcon(null, g, 0, 0);
                    g.dispose();
                    return img;
                } else {
                    return resize(ImageIO.read(url), ICON_SIZE, ICON_SIZE);
                }
            } catch (Exception e) { return null; }
        });
    }

    private Image resize(BufferedImage img, int w, int h) {
        var resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        var g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, w, h, null);
        g.dispose();
        return resized;
    }

    @Getter @Setter
    private static class DockItem {
        private final AiConfiguration.AiConfig config;
        private final int originalIndex;
        private float currentWidth = 0f;
        private float targetWidth = 0f;
        private boolean isSelected = false;
        private boolean isHovered = false;

        public DockItem(AiConfiguration.AiConfig config, int index) {
            this.config = config;
            this.originalIndex = index;
        }
    }
}
