package org.phoebus.hdf.util;

import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class HDFConfigure {

    private static AtomicBoolean initialized = new AtomicBoolean(false);

    static final Logger logger = Logger.getLogger(HDFConfigure.class.getName());

    public static void initialize() {
        if (!initialized.get())
        {
            // Load the preferences and configure the dll path
            try {
                addLibraryPath(Paths.get(HDFPreferences.hdf_lib_path).toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            initialized.set(true);
        }
    }

    /**
     * Add the hdf native binaries to the classloader path.
     * @param pathToAdd The path to the hdf dlls and binaries
     * @throws Exception
     */
    public static void addLibraryPath(String pathToAdd) throws Exception {
        logger.info("configuring the class loader path to include " + pathToAdd);
        Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
        usrPathsField.setAccessible(true);

        String[] paths = (String[]) usrPathsField.get(null);

        for (String path : paths)
            if (path.equals(pathToAdd))
                return;

        String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
        newPaths[newPaths.length - 1] = pathToAdd;
        usrPathsField.set(null, newPaths);
    }
}
