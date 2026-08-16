package to.sparkapp.app.ui.settings.components;

import to.sparkapp.app.ui.Theme;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class AnimatedSettingsButton extends Button {

    private static final double HEIGHT = 32;
    private static final int RADIUS = 9;

    private final String fontCss;
    private final Color normalBg = Theme.BG_POPUP;
    private final Color hoverBg = Theme.BG_HOVER;
    private final Color normalBorder = Theme.BORDER;
    private final Color hoverBorder = Theme.withAlpha(Theme.ACCENT, 0.55);

    private final SimpleDoubleProperty hoverT = new SimpleDoubleProperty(0.0);
    private final Timeline hoverAnim = new Timeline();

    public AnimatedSettingsButton(String text, Runnable action) {
        super(text);
        this.setCursor(Cursor.HAND);

        this.setPrefHeight(HEIGHT);
        this.setMinHeight(HEIGHT);
        this.setMaxHeight(HEIGHT);
        this.setPadding(new Insets(0, 16, 0, 16));
        this.setMaxWidth(Region.USE_PREF_SIZE);

        fontCss = String.format("-fx-font-family: '%s'; -fx-font-size: 13px;",
                Theme.FONT_SETTINGS.getFamily());

        hoverT.addListener((obs, old, val) -> applyStyle(val.doubleValue()));
        applyStyle(0.0);

        this.setOnMouseEntered(e -> animateTo(1.0));
        this.setOnMouseExited(e -> animateTo(0.0));

        this.setOnAction(e -> {
            e.consume();
            if (action != null) action.run();
        });
    }

    private void animateTo(double target) {
        hoverAnim.stop();
        hoverAnim.getKeyFrames().clear();
        hoverAnim.getKeyFrames().add(new KeyFrame(Duration.millis(150),
                new KeyValue(hoverT, target, Interpolator.EASE_OUT)
        ));
        hoverAnim.play();
    }

    private void applyStyle(double t) {
        this.setStyle(String.format(
                "-fx-background-color: %s; -fx-border-color: %s; -fx-border-width: 1px; " +
                        "-fx-text-fill: %s; -fx-background-radius: %spx; -fx-border-radius: %spx; %s",
                Theme.toHex(Theme.lerp(normalBg, hoverBg, t)),
                Theme.toHexWithAlpha(Theme.lerp(normalBorder, hoverBorder, t)),
                Theme.toHex(Theme.TEXT_PRIMARY),
                RADIUS, RADIUS, fontCss
        ));
    }
}
