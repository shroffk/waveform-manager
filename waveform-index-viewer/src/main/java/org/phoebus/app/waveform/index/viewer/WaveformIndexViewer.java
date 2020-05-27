package org.phoebus.app.waveform.index.viewer;

import javafx.fxml.FXMLLoader;
import org.phoebus.framework.spi.AppDescriptor;
import org.phoebus.framework.spi.AppInstance;
import org.phoebus.ui.docking.DockItem;
import org.phoebus.ui.docking.DockPane;

import java.io.IOException;

public class WaveformIndexViewer implements AppInstance {

    private DockItem tab;
    private final WaveformIndexViewerApp app;

    public WaveformIndexViewer(WaveformIndexViewerApp app) {
        this.app = app;
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(this.getClass().getResource("WaveformIndexViewer.fxml"));
            loader.load();



            tab = new DockItem(this, loader.getRoot());
            DockPane.getActiveDockPane().addTab(tab);
        } catch (IOException e) {

        }
    }

    @Override
    public AppDescriptor getAppDescriptor() {
        return app;
    }
}
