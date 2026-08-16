package to.sparkapp.app.ui.topbar.components;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import to.sparkapp.app.ui.Theme;

public class AiDockScrollArrow extends StackPane {
    private static final int ARROW_WIDTH = 22;
    private static final double SCROLL_STEP = 0.12;
    private static final double HOLD_SCROLL_RATE = 0.010;

    public AiDockScrollArrow(boolean isLeft, AiDock dock) {
        this.setPrefWidth(ARROW_WIDTH);
        this.setMinWidth(ARROW_WIDTH);
        this.setMaxWidth(ARROW_WIDTH);
        this.setAlignment(Pos.CENTER);
        this.setCursor(Cursor.HAND);

        var bg = new Rectangle();
        bg.setArcWidth(8);
        bg.setArcHeight(8);
        bg.setFill(Theme.BG_HOVER);
        bg.setWidth(ARROW_WIDTH);
        // Unmanaged on purpose: its height is driven by this pane's height, so letting
        // it count towards the pane's preferred size would make the two grow each other.
        // On a fractionally scaled monitor every layout pass rounds the size up to the
        // next physical pixel, and the top bar creeps taller with every hover animation.
        bg.setManaged(false);

        var tri = new Polygon();
        if (isLeft) {
            tri.getPoints().addAll(-3.5, 0.0, 3.5, -5.0, 3.5, 5.0);
        } else {
            tri.getPoints().addAll(3.5, 0.0, -3.5, -5.0, -3.5, 5.0);
        }
        tri.setFill(Theme.TEXT_SECONDARY);

        this.getChildren().addAll(bg, tri);

        this.heightProperty().addListener((obs, old, h) -> bg.setHeight(h.doubleValue()));

        this.setOnMouseEntered(e -> tri.setFill(Theme.TEXT_PRIMARY));
        this.setOnMouseExited(e -> tri.setFill(Theme.TEXT_SECONDARY));

        var holdTimeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            double step = isLeft ? -HOLD_SCROLL_RATE : HOLD_SCROLL_RATE;
            var scrollPane = dock.getScrollPane();
            scrollPane.setHvalue(Math.max(0.0, Math.min(1.0, scrollPane.getHvalue() + step)));
            dock.refreshArrowOpacity();
        }));
        holdTimeline.setCycleCount(Animation.INDEFINITE);

        var holdDelay = new Timeline(new KeyFrame(Duration.millis(350), e -> holdTimeline.play()));

        this.setOnMousePressed(e -> {
            double step = isLeft ? -SCROLL_STEP : SCROLL_STEP;
            var scrollPane = dock.getScrollPane();
            scrollPane.setHvalue(Math.max(0.0, Math.min(1.0, scrollPane.getHvalue() + step)));
            dock.refreshArrowOpacity();
            holdDelay.playFromStart();
            e.consume();
        });
        this.setOnMouseReleased(e -> {
            holdDelay.stop();
            holdTimeline.stop();
        });
        this.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            holdDelay.stop();
            holdTimeline.stop();
            tri.setFill(Theme.TEXT_SECONDARY);
        });
    }
}
