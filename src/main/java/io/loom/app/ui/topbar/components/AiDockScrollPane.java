package io.loom.app.ui.topbar.components;

import javax.swing.*;
import java.awt.*;

class AiDockScrollPane extends JScrollPane {

    AiDockScrollPane(JComponent content) {
        super(content);

        this.setOpaque(false);
        this.getViewport().setOpaque(false);
        this.setBorder(null);
        this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        this.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 3));
        this.getHorizontalScrollBar().setUnitIncrement(16);
    }
}
