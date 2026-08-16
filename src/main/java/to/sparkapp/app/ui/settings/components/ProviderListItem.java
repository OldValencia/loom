package to.sparkapp.app.ui.settings.components;

import to.sparkapp.app.config.AiConfiguration;
import to.sparkapp.app.ui.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * A single provider inside the providers card: colour dot, name, URL and actions.
 */
class ProviderListItem extends HBox {

    private static final double DOT_RADIUS = 5;

    ProviderListItem(AiConfiguration.AiConfig provider, Runnable onEdit, Runnable onDelete) {
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(12);
        setMaxWidth(Double.MAX_VALUE);
        setPadding(new Insets(10, 12, 10, 12));
        setMinHeight(52);

        getChildren().addAll(createColorDot(provider.color()), createInfoPanel(provider));

        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().add(spacer);

        getChildren().add(createActionButtons(onEdit, onDelete));

        SettingsRow.applyHoverHighlight(this);
    }

    private Node createColorDot(String colorHex) {
        var dot = new Circle(DOT_RADIUS);
        try {
            dot.setFill(Color.web(colorHex));
        } catch (Exception e) {
            dot.setFill(Theme.ACCENT);
        }

        var container = new VBox(dot);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(0, 0, 0, 4));
        return container;
    }

    private Node createInfoPanel(AiConfiguration.AiConfig provider) {
        var nameLabel = new Label(provider.name());
        nameLabel.setFont(Theme.FONT_SETTINGS_TITLE);
        nameLabel.setTextFill(Theme.TEXT_PRIMARY);

        var urlLabel = new Label(provider.url());
        urlLabel.setFont(Theme.FONT_SETTINGS_HINT);
        urlLabel.setTextFill(Theme.TEXT_TERTIARY);

        var infoPanel = new VBox(3, nameLabel, urlLabel);
        infoPanel.setAlignment(Pos.CENTER_LEFT);
        return infoPanel;
    }

    private Node createActionButtons(Runnable onEdit, Runnable onDelete) {
        var actionsPanel = new HBox(14);
        actionsPanel.setAlignment(Pos.CENTER_RIGHT);

        actionsPanel.getChildren().addAll(
                new ProvidersListTextButton("Edit", Theme.TEXT_SECONDARY, Theme.ACCENT, onEdit),
                new ProvidersListTextButton("Delete", Theme.TEXT_SECONDARY, Theme.DANGER, onDelete)
        );

        return actionsPanel;
    }
}
