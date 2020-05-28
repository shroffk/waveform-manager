package org.phoebus.app.waveform.index.viewer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.phoebus.ui.javafx.ApplicationWrapper;

import java.io.File;
import java.net.URLEncoder;

public class WaveformIndexViewerDemo extends ApplicationWrapper {

    public static void main(String[] args) {
        launch(WaveformIndexViewerDemo.class, args);
    }


    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource("WaveformIndexViewer.fxml"));
        loader.load();

        WaveformIndexViewerController controller = loader.getController();

        Parent root = loader.getRoot();
        stage.setScene(new Scene(root, 400, 400));
        stage.show();

    }
}
