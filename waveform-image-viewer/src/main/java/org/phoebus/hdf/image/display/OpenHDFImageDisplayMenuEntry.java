package org.phoebus.hdf.image.display;

import org.phoebus.framework.workbench.ApplicationService;
import org.phoebus.ui.spi.MenuEntry;

public class OpenHDFImageDisplayMenuEntry implements MenuEntry {

    @Override
    public String getName() {
        return HDFImageDisplayApp.DISPLAY_NAME;
    }

    @Override
    public String getMenuPath() {
        return "Utility";
    }

    @Override
    public Void call() throws Exception {
        ApplicationService.findApplication(HDFImageDisplayApp.NAME).create();
        return null;
    }
}
