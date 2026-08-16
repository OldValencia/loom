package to.sparkapp.app.ui.dialogs.components;

import to.sparkapp.app.ui.Theme;
import javafx.geometry.Insets;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

public class ProviderMainPanel extends BorderPane {

    /** Space the dialog scene has to reserve around the panel for the shadow. */
    public static final double SHADOW_PADDING = 20;

    public ProviderMainPanel() {
        this.setPadding(new Insets(20, 24, 20, 24));
        this.setStyle(
                "-fx-background-color: " + Theme.toHex(Theme.BG_BAR) + "; " +
                        "-fx-background-radius: 14; " +
                        "-fx-border-color: " + Theme.toHex(Theme.BORDER) + "; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 14;"
        );

        var shadow = new DropShadow();
        shadow.setRadius(26);
        shadow.setSpread(0.08);
        shadow.setOffsetY(8);
        shadow.setColor(Color.rgb(0, 0, 0, 0.55));
        this.setEffect(shadow);
    }
}
