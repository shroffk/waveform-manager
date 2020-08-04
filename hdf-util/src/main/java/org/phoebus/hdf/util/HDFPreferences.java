package org.phoebus.hdf.util;

import org.phoebus.framework.preferences.PreferencesReader;

import java.util.logging.Level;
import static org.phoebus.hdf.util.HDFConfigure.logger;
/**
 * A basic class for handling the preferences for the hdf display application
 * @author Kunal Shroff
 */
public class HDFPreferences {

    // Path to the local hdfviewer lib
    public static final String hdf_lib_path;

    static {
        final PreferencesReader prefs = new PreferencesReader(HDFPreferences.class, "/hdf.properties");
        hdf_lib_path = prefs.get("hdf_lib_path");

        if (hdf_lib_path == null) {
            logger.log(Level.INFO, "No valid path to hdf binaries found");
        }
    }
}
