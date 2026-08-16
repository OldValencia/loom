package to.sparkapp.app.ui.settings.components;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import to.sparkapp.app.config.AiConfiguration;

/**
 * Confirms and performs "restore the default provider list". Not a node — it is
 * wired to the "Reset" action in the providers section header.
 */
public class ProvidersResetHandler implements Runnable {

    private final AiConfiguration aiConfiguration;
    private final Runnable onProvidersChanged;

    public ProvidersResetHandler(AiConfiguration aiConfiguration, Runnable onProvidersChanged) {
        this.aiConfiguration = aiConfiguration;
        this.onProvidersChanged = onProvidersChanged;
    }

    @Override
    public void run() {
        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Configuration");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure? This will delete all custom providers and icons.");

        var stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);

        var result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            aiConfiguration.resetToDefaults();
            if (onProvidersChanged != null) {
                onProvidersChanged.run();
            }
        }
    }
}
