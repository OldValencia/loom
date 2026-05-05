package to.sparkapp.app.ui.topbar;

import to.sparkapp.app.config.AiConfiguration;
import to.sparkapp.app.config.AppPreferences;
import to.sparkapp.app.ui.webview.FxWebViewPane;
import to.sparkapp.app.ui.topbar.components.*;
import to.sparkapp.app.windows.SettingsWindow;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class TopBarArea extends GradientPanel {

    private final Stage frame;
    private final SettingsWindow settingsWindow;

    private double xOffset = 0;
    private double yOffset = 0;

    private double startScreenX = 0;
    private double startScreenY = 0;
    private boolean isDragging = false;
    private static final double DRAG_THRESHOLD = 5.0;

    public TopBarArea(AiConfiguration aiConfiguration,
                      FxWebViewPane fxWebViewPane,
                      Stage frame,
                      SettingsWindow settingsWindow,
                      AppPreferences appPreferences,
                      Runnable onSettingsToggle,
                      Runnable onCloseWindow) {
        super();

        this.frame = frame;
        this.settingsWindow = settingsWindow;

        this.setPrefSize(frame.getWidth(), 48);
        this.setLeft(new LeftTopBarArea(aiConfiguration, fxWebViewPane, appPreferences));
        this.setRight(new RightTopBarArea(fxWebViewPane, onSettingsToggle, onCloseWindow));

        setupDragging();

        if (!aiConfiguration.getConfigurations().isEmpty()) {
            this.updateAccentColor(Color.web(aiConfiguration.getConfigurations().getFirst().color()));
        }
    }

    private void setupDragging() {
        this.setOnMousePressed(e -> {
            if (!isDraggableTarget((Node) e.getTarget())) {
                isDragging = false;
                return;
            }

            xOffset = e.getSceneX();
            yOffset = e.getSceneY();

            startScreenX = e.getScreenX();
            startScreenY = e.getScreenY();
            isDragging = true;

            if (settingsWindow != null && settingsWindow.isOpen()) {
                settingsWindow.close();
            }
        });

        this.setOnMouseDragged(e -> {
            if (!isDragging) return;

            double deltaX = Math.abs(e.getScreenX() - startScreenX);
            double deltaY = Math.abs(e.getScreenY() - startScreenY);

            if (deltaX > DRAG_THRESHOLD || deltaY > DRAG_THRESHOLD) {
                frame.setX(e.getScreenX() - xOffset);
                frame.setY(e.getScreenY() - yOffset);
            }
        });

        this.setOnMouseReleased(e -> isDragging = false);
    }

    private static boolean isDraggableTarget(Node node) {
        while (node != null) {
            if (node instanceof ButtonBase
                    || node instanceof DockItemNode
                    || node instanceof AnimatedIconButton
                    || node instanceof ZoomButton
                    || node instanceof ScrollPane
                    || node instanceof AiDock) {
                return false;
            }
            node = node.getParent();
        }
        return true;
    }
}
