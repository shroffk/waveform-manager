package org.phoebus.hdf.image.display;

import javafx.scene.Parent;
import javafx.scene.image.Image;
import org.phoebus.ui.javafx.Screenshot;

public class SelectionInfo {

    private final String name;
    private final Parent parent;

    public SelectionInfo(String name, Parent parent) {
        this.name = name;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public Image getImage()
    {
        return Screenshot.imageFromNode(parent);
    }
}