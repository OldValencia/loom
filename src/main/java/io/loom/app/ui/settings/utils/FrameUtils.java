package io.loom.app.ui.settings.utils;

import javax.swing.*;
import java.awt.*;

public class FrameUtils {
    public static Frame getOwnerFrame(Component currentComponent) {
        var window = SwingUtilities.getWindowAncestor(currentComponent);
        if (window instanceof JWindow jWindow && jWindow.getOwner() instanceof Frame frame) {
            return frame;
        }
        if (window instanceof Frame frame) {
            return frame;
        }
        return null;
    }
}
