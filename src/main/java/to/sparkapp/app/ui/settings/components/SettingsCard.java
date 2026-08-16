package to.sparkapp.app.ui.settings.components;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import to.sparkapp.app.ui.Theme;

/**
 * Rounded, slightly elevated container that groups related settings rows and
 * separates them with hairlines — the card pattern used by modern system
 * preference panes.
 */
public class SettingsCard extends VBox {

    private static final Insets CARD_PADDING = new Insets(6);
    private static final Insets SEPARATOR_MARGIN = new Insets(0, 10, 0, 10);

    public SettingsCard() {
        setFillWidth(true);
        setMaxWidth(Double.MAX_VALUE);
        setPadding(CARD_PADDING);
        setStyle("""
                    -fx-background-color: %s;
                    -fx-background-radius: %spx;
                    -fx-border-color: %s;
                    -fx-border-width: 1px;
                    -fx-border-radius: %spx;
                """.formatted(
                Theme.toHex(Theme.BG_CARD),
                Theme.CARD_RADIUS,
                Theme.toHex(Theme.BORDER),
                Theme.CARD_RADIUS));
    }

    /**
     * Appends a row, preceded by a hairline when it is not the first one.
     */
    public void addRow(Node row) {
        if (!getChildren().isEmpty()) {
            getChildren().add(createSeparator());
        }
        getChildren().add(row);
    }

    public void clearRows() {
        getChildren().clear();
    }

    private Region createSeparator() {
        var separator = new Region();
        separator.setMinHeight(1);
        separator.setPrefHeight(1);
        separator.setMaxHeight(1);
        separator.setStyle("-fx-background-color: " + Theme.toHex(Theme.SEPARATOR) + ";");
        VBox.setMargin(separator, SEPARATOR_MARGIN);
        return separator;
    }
}
