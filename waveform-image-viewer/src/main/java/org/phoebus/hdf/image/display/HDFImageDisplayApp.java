package org.phoebus.hdf.image.display;

import org.phoebus.framework.spi.AppInstance;
import org.phoebus.framework.spi.AppResourceDescriptor;
import org.phoebus.hdf.util.HDFConfigure;

import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HDFImageDisplayApp implements AppResourceDescriptor {

    public static final Logger logger = Logger.getLogger(HDFImageDisplayApp.class.getName());

    static final String HDF_EXTENSION = "h5";
    static final List<String> SupportedExtensions = List.of(HDF_EXTENSION);

    public static final String NAME = "hdf_image_display";
    public static final String DISPLAY_NAME = "HDF Image Display View";


    @Override
    public void start() {
        // Load the preferences and configure the dll path
        try {
            HDFConfigure.initialize();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to configure hdf binaries", e);
        }
    }

    @Override
    public List<String> supportedFileExtentions() {
        return SupportedExtensions;
    }

    @Override
    public AppInstance create(URI resource) {
        HDFImageDisplay display = new HDFImageDisplay(this);
        display.setResource(resource);
        return display;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }


    @Override
    public AppInstance create() {
        return new HDFImageDisplay(this);
    }

    @Override
    public void stop() {

    }
}
