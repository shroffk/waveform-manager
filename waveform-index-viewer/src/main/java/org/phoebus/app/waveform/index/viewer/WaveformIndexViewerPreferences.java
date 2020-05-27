package org.phoebus.app.waveform.index.viewer;

import java.util.logging.Level;

import org.phoebus.framework.preferences.PreferencesReader;

/**
 * A basic class for handling the preferences for the hdf display application
 *
 * @author Kunal Shroff
 */
public class WaveformIndexViewerPreferences {

    // waveform index service
    public static final String waveform_index_url;

    static {
        final PreferencesReader prefs = new PreferencesReader(WaveformIndexViewerPreferences.class, "/waveform_index_viewer.properties");
        waveform_index_url = prefs.get("service.url");

        if (waveform_index_url == null) {
            WaveformIndexViewerApp.logger.log(Level.INFO, "waveform index service url not set");
        }
    }
}
