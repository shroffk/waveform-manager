package org.phoebus.hdf.image.display;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.phoebus.hdf.util.HDFPreferences;
import org.phoebus.ui.javafx.ApplicationWrapper;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class HDFImageDisplayViewDemo extends ApplicationWrapper {

    public static void main(String[] args) {
        launch(HDFImageDisplayViewDemo.class, args);
    }

    //TODO need to make a reasonably sized test file and include it in the test resources
    private static String FILE = "C:\\HDF_Group\\HDF_files\\hdf-image-demo.h5";

    @Override
    public void start(Stage stage) throws Exception {

        // Load the preferences and configure the dll path
        String dllPath = (HDFPreferences.hdf_lib_path);

        addLibraryPath(dllPath);

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource("HDFImageDisplay.fxml"));
        loader.load();

        HDFImageDisplayController controller = loader.getController();
        controller.setFile(new File(FILE));

        Parent root = loader.getRoot();
        stage.setScene(new Scene(root, 400, 400));
        stage.show();
        stage.setOnCloseRequest((event) ->
        {
            controller.stop();
        });
    }

    public static void addLibraryPath(String pathToAdd) throws Exception {
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
