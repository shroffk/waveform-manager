package org.phoebus.hdf.display;

import org.phoebus.framework.spi.AppInstance;
import org.phoebus.framework.spi.AppResourceDescriptor;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

public class HDFDisplayApp implements AppResourceDescriptor {

    static final Logger logger = Logger.getLogger(HDFDisplayApp.class.getName());
    public static final String NAME = "hdf_display";
    public static final String DISPLAYNAME = "HDF Display View";


    @Override
    public void start() {

    }

    @Override
    public List<String> supportedFileExtentions() {
        return null;
    }

    @Override
    public AppInstance create(URI resource) {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }


    @Override
    public AppInstance create() {
        return null;
    }

    @Override
    public void stop() {

    }
}
