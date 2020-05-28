package org.phoebus.app.waveform.index.viewer.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.io.IOException;
import java.util.Collection;

/**
 * A dialog for removing a WaveformFileTag to a list of waveform index files
 * 
 * @author Kunal Shroff
 *
 */
public class RemoveTagDialog extends Dialog<String> {

    public RemoveTagDialog(final Node parent, final Collection<String> tags) {
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        setResizable(true);
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource("SelectEntity.fxml"));
        try {
            getDialogPane().setContent(loader.load());
            SelectEntityController controller = loader.getController();
            controller.setAvaibleOptions(tags);
            setResultConverter(button -> {
                return button == ButtonType.OK
                        ? controller.getSelectedOption()
                        : null;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
