package org.phoebus.hdf.display;

import org.phoebus.framework.spi.AppInstance;
import org.phoebus.framework.spi.AppResourceDescriptor;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

public class HDFDisplayApp implements AppResourceDescriptor {

    static final Logger logger = Logger.getLogger(HDFDisplayApp.class.getName());

    static final String HDF_EXTENSION = "h5";
    static final List<String> SupportedExtensions = List.of(HDF_EXTENSION);

    public static final String NAME = "hdf_display";
    public static final String DISPLAYNAME = "HDF Display View";


    @Override
    public void start() {
        // Load the preferences and configure the dll path
        System.setProperty("java.library.path", HDFDisplayPreferences.hdf_lib_path);
    }

    @Override
    public List<String> supportedFileExtentions() {
        return SupportedExtensions;
    }

    @Override
    public AppInstance create(URI resource) {
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return DISPLAYNAME;
    }


    @Override
    public AppInstance create() {
        return null;
    }

    @Override
    public void stop() {

    }
}
