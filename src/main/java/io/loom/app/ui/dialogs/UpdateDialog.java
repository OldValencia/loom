package io.loom.app.ui.dialogs;

import javax.swing.*;
import java.awt.*;

public class UpdateDialog extends JOptionPane {
    public UpdateDialog(String version, Component parent) {
        super("New version " + version + " is available! Download now?", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);

        var title = "Update Available";
        var dialog = this.createDialog(parent, title);

        dialog.setAlwaysOnTop(true);
        dialog.setModal(true);
        dialog.setVisible(true);
    }
}
