package org.phoebus.app.waveform.index.viewer;

import org.phoebus.framework.spi.AppInstance;
import org.phoebus.framework.spi.AppResourceDescriptor;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class WaveformIndexViewerApp implements AppResourceDescriptor {

    static final Logger logger = Logger.getLogger(WaveformIndexViewerApp.class.getName());

    public static final String NAME = "waveform_index_viewer";
    public static final String DISPLAY_NAME = "Waveform Index View";
    private URI serviceURL;

    @Override
    public void start() {
    }

    @Override
    public List<String> supportedFileExtentions() {
        return Collections.emptyList();
    }

    @Override
    public AppInstance create(URI resource) {
        return new WaveformIndexViewer(this);
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
        return new WaveformIndexViewer(this);
    }

    @Override
    public void stop() {

    }

}
