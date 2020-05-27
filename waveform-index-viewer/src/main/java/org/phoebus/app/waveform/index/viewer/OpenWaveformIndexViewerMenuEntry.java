package org.phoebus.app.waveform.index.viewer;

import org.phoebus.framework.workbench.ApplicationService;
import org.phoebus.ui.spi.MenuEntry;

public class OpenWaveformIndexViewerMenuEntry implements MenuEntry {

    @Override
    public String getName() {
        return WaveformIndexViewerApp.DISPLAY_NAME;
    }

    @Override
    public String getMenuPath() {
        return "Utility";
    }

    @Override
    public Void call() throws Exception {
        ApplicationService.findApplication(WaveformIndexViewerApp.NAME).create();
        return null;
    }
}
