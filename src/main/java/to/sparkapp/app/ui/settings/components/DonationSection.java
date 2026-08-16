package to.sparkapp.app.ui.settings.components;

import to.sparkapp.app.ui.Theme;
import to.sparkapp.app.utils.UrlUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Support banner shown at the top of the settings: a short pitch on the left,
 * donation links on the right.
 */
public class DonationSection extends HBox {

    public DonationSection() {
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(12);
        setMaxWidth(Double.MAX_VALUE);
        setPadding(new Insets(14, 14, 14, 16));
        setStyle("""
                    -fx-background-color: %s;
                    -fx-background-radius: %spx;
                    -fx-border-color: %s;
                    -fx-border-width: 1px;
                    -fx-border-radius: %spx;
                """.formatted(
                Theme.toHex(Theme.BG_CARD),
                Theme.CARD_RADIUS,
                Theme.toHexWithAlpha(Theme.withAlpha(Theme.ACCENT, 0.35)),
                Theme.CARD_RADIUS));

        getChildren().add(buildPitch());

        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);

        var coffeeColor = Color.rgb(255, 200, 0);
        var bmcBtn = new ColorfulButton("☕ Buy me a coffee", coffeeColor,
                () -> UrlUtils.openLink("https://buymeacoffee.com/oldvalencia"));

        var kofiColor = Color.rgb(255, 94, 91);
        var kofiBtn = new ColorfulButton("❤ Ko-Fi", kofiColor,
                () -> UrlUtils.openLink("https://ko-fi.com/oldvalencia"));

        var buttons = new HBox(8, bmcBtn, kofiBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        getChildren().add(buttons);
    }

    private VBox buildPitch() {
        var title = new Label("Enjoying Spark?");
        title.setFont(Theme.FONT_SETTINGS_TITLE);
        title.setTextFill(Theme.TEXT_PRIMARY);

        var subtitle = new Label("Support the development of the app");
        subtitle.setFont(Theme.FONT_SETTINGS_HINT);
        subtitle.setTextFill(Theme.TEXT_TERTIARY);

        var pitch = new VBox(3, title, subtitle);
        pitch.setAlignment(Pos.CENTER_LEFT);
        return pitch;
    }
}
