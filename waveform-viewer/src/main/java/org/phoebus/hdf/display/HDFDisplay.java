package org.phoebus.hdf.display;

import javafx.fxml.FXMLLoader;
import org.phoebus.framework.spi.AppDescriptor;
import org.phoebus.framework.spi.AppInstance;
import org.phoebus.ui.docking.DockItem;
import org.phoebus.ui.docking.DockPane;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class HDFDisplay implements AppInstance {

    private final AppDescriptor app;

    private DockItem tab;
    private HDFDisplayController controller;

    public HDFDisplay(AppDescriptor app) {
        this.app = app;
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(this.getClass().getResource("HDFDisplay.fxml"));
            loader.load();

            controller = loader.getController();

            tab = new DockItem(this, loader.getRoot());
            DockPane.getActiveDockPane().addTab(tab);
        } catch (IOException e) {

        }
    }

    public void setResource(URI resource) {
        controller.setFile(new File(resource.getPath()));
    }

    @Override
    public AppDescriptor getAppDescriptor() {
        return app;
    }
}
