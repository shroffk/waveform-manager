package org.phoebus.hdf.display;

import java.util.logging.Level;

import org.phoebus.framework.preferences.PreferencesReader;
import static org.phoebus.hdf.display.HDFDisplayApp.logger;
/**
 * A basic class for handling the preferences for the hdf display application
 * @author Kunal Shroff
 */
public class HDFDisplayPreferences {

    // Path to the local hdfviewer lib
    public static final String hdf_lib_path;

    static {
        final PreferencesReader prefs = new PreferencesReader(HDFDisplayPreferences.class, "/hdf_display.properties");
        hdf_lib_path = prefs.get("hdf_lib_path");

        if (hdf_lib_path == null) {
            logger.log(Level.INFO, "No valid path to hdf binaries found");
        }
    }
}
