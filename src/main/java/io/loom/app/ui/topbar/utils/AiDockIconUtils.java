package io.loom.app.ui.topbar.utils;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import io.loom.app.config.AiConfiguration;
import io.loom.app.ui.topbar.components.AiDock;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

@Slf4j
public class AiDockIconUtils {

    public static void preloadIcon(AiConfiguration.AiConfig cfg, File userIconsDir) {
        if (cfg.icon() == null) {
            return;
        }

        AiDock.ICON_CACHE.computeIfAbsent(cfg.icon(), key -> {
            try {
                var resourceUrl = AiDock.class.getResource("/icons/" + key);
                if (resourceUrl != null) {
                    if (key.toLowerCase().endsWith(".svg")) {
                        var icon = new FlatSVGIcon(resourceUrl);
                        return renderSvg(icon);
                    } else {
                        return resize(ImageIO.read(resourceUrl));
                    }
                }

                var userIconFile = new File(userIconsDir, key);
                if (userIconFile.exists()) {
                    if (key.toLowerCase().endsWith(".svg")) {
                        var icon = new FlatSVGIcon(userIconFile);
                        return renderSvg(icon);
                    } else {
                        return resize(ImageIO.read(userIconFile));
                    }
                }

                log.warn("Icon not found: {} (searched in resources and {})", key, userIconsDir.getAbsolutePath());
                return createPlaceholderIcon();

            } catch (Exception e) {
                log.error("Error while trying to preload icon for {}", cfg.name(), e);
                return createPlaceholderIcon();
            }
        });
    }

    private static Image renderSvg(FlatSVGIcon icon) {
        var scaledIcon = icon.derive(AiDock.ICON_SIZE, AiDock.ICON_SIZE);
        var img = new BufferedImage(AiDock.ICON_SIZE, AiDock.ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
        var g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        scaledIcon.paintIcon(null, g, 0, 0);
        g.dispose();
        return img;
    }

    private static Image createPlaceholderIcon() {
        var img = new BufferedImage(AiDock.ICON_SIZE, AiDock.ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
        var g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(100, 100, 100));
        g.fillOval(2, 2, AiDock.ICON_SIZE - 4, AiDock.ICON_SIZE - 4);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        var fm = g.getFontMetrics();
        var text = "?";
        var x = (AiDock.ICON_SIZE - fm.stringWidth(text)) / 2;
        var y = (AiDock.ICON_SIZE + fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(text, x, y);

        g.dispose();
        return img;
    }

    private static Image resize(BufferedImage img) {
        var resized = new BufferedImage(AiDock.ICON_SIZE, AiDock.ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
        var g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(img, 0, 0, AiDock.ICON_SIZE, AiDock.ICON_SIZE, null);
        g.dispose();
        return resized;
    }
}
