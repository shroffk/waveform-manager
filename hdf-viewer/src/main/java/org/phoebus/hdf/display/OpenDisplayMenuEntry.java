package org.phoebus.hdf.display;

import org.phoebus.framework.workbench.ApplicationService;
import org.phoebus.ui.spi.MenuEntry;

public class OpenDisplayMenuEntry implements MenuEntry {

    @Override
    public String getName() {
        return HDFDisplayApp.DISPLAY_NAME;
    }

    @Override
    public String getMenuPath() {
        return "Utility";
    }

    @Override
    public Void call() throws Exception {
        ApplicationService.findApplication(HDFDisplayApp.NAME).create();
        return null;
    }
}
