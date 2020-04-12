package org.phoebus.hdf.display;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.phoebus.ui.javafx.ApplicationWrapper;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Arrays;

public class HDFDisplayViewDemo extends ApplicationWrapper {

    public static void main(String[] args) {
        launch(HDFDisplayViewDemo.class, args);
    }
    //-Djava.library.path=C:\hdf5\hdf-app\lib\hdfviewer
    private static String FILE = "C:\\hdf5\\PS-20200131-11%3A43%3A08.391806.h5";

    @Override
    public void start(Stage stage) throws Exception {

        // Load the preferences and configure the dll path
        String dllPath = "C:\\hdf5\\hdf-app\\lib\\hdfviewer";
//        System.setProperty("java.library.path", dllPath);

        System.out.println("SET Property...............");
//        addLibraryPath(dllPath);

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource("HDFDisplay.fxml"));
        loader.load();

        HDFDisplayController controller = loader.getController();
        controller.setFile(new File(FILE));

        Parent root = loader.getRoot();
        stage.setScene(new Scene(root, 400, 400));
        stage.show();

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
