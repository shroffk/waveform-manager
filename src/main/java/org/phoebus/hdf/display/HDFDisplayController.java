package org.phoebus.hdf.display;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
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
    TreeTableView<HDFDisplayTreeNode> treeTableView;
    @FXML
    TreeTableColumn name;
    @FXML
    TreeTableColumn plot;
    @FXML
    TreeTableColumn timeStamp;

    // HDF file
    private File file;

    @FXML
    public void initialize() {
        this.file = new File("C:\\hdf5\\Test.h5");

        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource("HDFTreeCell.fxml"));

        name.setCellValueFactory(
                (Callback<TreeTableColumn.CellDataFeatures<HDFDisplayTreeNode, HDFDisplayTreeNode>, ObservableValue<HDFDisplayTreeNode>>) p ->
                        new SimpleObjectProperty<HDFDisplayTreeNode>(p.getValue().getValue()));

        name.setCellFactory(new Callback<TreeTableColumn, TreeTableCell>() {
            @Override
            public TreeTableCell call(TreeTableColumn param) {
                return new TreeTableCell<HDFDisplayTreeNode, HDFDisplayTreeNode>() {

                    @Override
                    protected void updateItem(HDFDisplayTreeNode item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setText(item.getName());
                        }
                    }
                };
            }
        });

        plot.setCellValueFactory(
                (Callback<TreeTableColumn.CellDataFeatures<HDFDisplayTreeNode, HDFDisplayTreeNode>, ObservableValue<HDFDisplayTreeNode>>) p ->
                        new SimpleObjectProperty<HDFDisplayTreeNode>(p.getValue().getValue()));
        plot.setCellFactory(new Callback<TreeTableColumn, TreeTableCell>() {
            @Override
            public TreeTableCell call(TreeTableColumn param) {
                return new TreeTableCell<HDFDisplayTreeNode, HDFDisplayTreeNode>() {

                    @Override
                    protected void updateItem(HDFDisplayTreeNode item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            if(item.isLeaf()){
                                CheckBox checkBox = new CheckBox();
                                checkBox.selectedProperty().bindBidirectional(item.isPlotted());
                                setGraphic(checkBox);
                            }
                        }
                    }
                };
            }
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
            treeTableView.setRoot(root);
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
            if (item != null) {
                controller.checkBox.setText(item.getName());
                controller.checkBox.selectedProperty().bindBidirectional(item.isPlotted());
            }
        }

    }
}
