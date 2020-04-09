package org.phoebus.hdf.display;

import javafx.fxml.FXMLLoader;
import org.phoebus.framework.spi.AppDescriptor;
import org.phoebus.framework.spi.AppInstance;
import org.phoebus.ui.docking.DockItem;
import org.phoebus.ui.docking.DockPane;

import java.io.IOException;

public class HDFDisplay implements AppInstance {

    private DockItem tab;

    public HDFDisplay() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(this.getClass().getResource("HDFDisplay.fxml"));
            loader.load();

            tab = new DockItem(this, loader.getRoot());
            DockPane.getActiveDockPane().addTab(tab);
        } catch (IOException e) {

        }
    }

    @Override
    public AppDescriptor getAppDescriptor() {
        return null;
    }
}
