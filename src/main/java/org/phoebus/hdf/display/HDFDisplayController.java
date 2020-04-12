package org.phoebus.hdf.display;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;

import static org.phoebus.hdf.display.HDFFileProcessor.HDFDisplayTreeNode;
public class HDFDisplayController {

    // Plot

    @FXML
    TextField filter;

    @FXML
    TreeView<HDFDisplayTreeNode> treeView;

    // HDF file
    private File file;

    @FXML
    public void initialize() {
        this.file = new File("C:\\hdf5\\Test.h5");

        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource("HDFTreeCell.fxml"));

        treeView.setCellFactory( (treeView) -> {

            return new TreeCell<>(){
                @Override
                protected void updateItem(HDFDisplayTreeNode item, boolean empty) {
                    super.updateItem(item, empty);
                    if(empty) {
                        setGraphic(null);
                    } else {
                        try {
                            setGraphic(new HDFCell(item));
                        } catch (IOException e) {
                            setGraphic(new Label(e.getMessage()));
                        }
                    }
                }
            };
        });
        constructTree();
    }

    public void setFile(File file) {
        this.file = file;
        constructTree();
    }

    public void constructTree() {
        try {
            TreeItem root = HDFFileProcessor.processFile(this.file);
            treeView.setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class HDFCell extends AnchorPane {

        public HDFCell(HDFDisplayTreeNode item) throws IOException {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("HDFTreeCell.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.load();

            HDFTreeCellController controller = fxmlLoader.getController();
            controller.checkBox.setText(item.getName());
            controller.checkBox.selectedProperty().bindBidirectional(item.isPlotted());
        }

    }
}
