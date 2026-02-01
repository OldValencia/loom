package io.aipanel.app.ui;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.util.Arrays;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Theme {

    // background
    public static final Color BG_DEEP = new Color(22, 22, 24);
    public static final Color BG_BAR = new Color(28, 28, 30);
    public static final Color BG_POPUP = new Color(32, 32, 35);
    public static final Color BG_HOVER = new Color(42, 42, 46);

    // text
    public static final Color TEXT_PRIMARY = new Color(230, 230, 232);
    public static final Color TEXT_SECONDARY = new Color(160, 160, 165);

    // Accents/borders
    public static final Color ACCENT = new Color(100, 160, 255);
    public static final Color BORDER = new Color(50, 50, 54);
    public static final Color BORDER_LIGHT = new Color(56, 56, 64);

    // btn hovers
    public static final Color BTN_HOVER_SETTINGS = new Color(140, 140, 150);
    public static final Color BTN_HOVER_CLOSE = new Color(255, 80, 80);
    public static final Color BTN_RING = new Color(80, 80, 90);

    // fonts
    public static final String FONT_NAME = resolveFontName();
    public static final Font FONT_RIGHT_TOP_BAR_AREA = new Font("SansSerif", Font.PLAIN, 17);
    public static final Font FONT_SELECTOR = new Font(FONT_NAME, Font.PLAIN, 14);

    private static String resolveFontName() {
        var available = Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()
        );
        if (available.contains("SF Pro Display")) {
            return "SF Pro Display";
        }
        if (available.contains("SF Pro Text")) {
            return "SF Pro Text";
        }
        if (available.contains("Segoe UI")) {
            return "Segoe UI";
        }
        return "SansSerif";
    }
}
