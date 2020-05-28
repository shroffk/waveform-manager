package org.phoebus.hdf.display;

import hdf.object.h5.H5ScalarDS;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.util.Callback;
import org.csstudio.javafx.rtplot.LineStyle;
import org.csstudio.javafx.rtplot.PointType;
import org.csstudio.javafx.rtplot.RTTimePlot;
import org.csstudio.javafx.rtplot.Trace;
import org.csstudio.javafx.rtplot.TraceType;
import org.csstudio.javafx.rtplot.data.ArrayPlotDataProvider;
import org.csstudio.javafx.rtplot.data.SimpleDataItem;
import org.csstudio.javafx.rtplot.util.RGBFactory;
import org.phoebus.ui.application.Messages;
import org.phoebus.ui.application.PhoebusApplication;
import org.phoebus.ui.javafx.ImageCache;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.phoebus.hdf.display.HDFFileProcessor.HDFDisplayTreeNode;

public class HDFDisplayController {

    // Plot
    @FXML
    AnchorPane plotArea;
    @FXML
    final RTTimePlot rtTimePlot = new RTTimePlot(true);

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

    // Context menu
    private final MenuItem addPVs2Plot = new MenuItem("Add PVs to Plot", ImageCache.getImageView(PhoebusApplication.class, "/icons/add.png"));
    private final MenuItem removePVsFromPlot = new MenuItem("Remove PVs to Plot", ImageCache.getImageView(PhoebusApplication.class, "/icons/delete.png"));

//    private final Menu openWith = new Menu(Messages.OpenWith, ImageCache.getImageView(PhoebusApplication.class, "/icons/fldr_obj.png"));
    private final ContextMenu contextMenu = new ContextMenu();

    // HDF file
    private File file;

    @FXML
    public void initialize() {
        this.file = new File("C:\\hdf5\\Test.h5");

        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource("HDFTreeCell.fxml"));

        name.setCellValueFactory(
                (Callback<TreeTableColumn.CellDataFeatures<HDFDisplayTreeNode, HDFDisplayTreeNode>, ObservableValue<HDFDisplayTreeNode>>) p -> {
                    return new SimpleObjectProperty<HDFDisplayTreeNode>(p.getValue().getValue());
                });


        name.setCellFactory(new Callback<TreeTableColumn, TreeTableCell>() {
            @Override
            public TreeTableCell call(TreeTableColumn param) {
                return new TreeTableCell<HDFDisplayTreeNode, HDFDisplayTreeNode>() {

                    @Override
                    protected void updateItem(HDFDisplayTreeNode item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
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
                                final CheckBox checkBox = new CheckBox();
                                checkBox.selectedProperty().bindBidirectional(item.isPlotted());
                                item.isPlotted().addListener((observable, oldValue, newValue) -> {
                                    if (newValue.booleanValue()) {
                                        addToPlot(item);
                                    } else {
                                        remoteFromPlot(item);
                                    }
                                });
                                setGraphic(checkBox);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        });

        timeStamp.setCellValueFactory(
                (Callback<TreeTableColumn.CellDataFeatures<HDFDisplayTreeNode, HDFDisplayTreeNode>, ObservableValue<HDFDisplayTreeNode>>) p -> {
                    return new SimpleObjectProperty<HDFDisplayTreeNode>(p.getValue().getValue());
                });


        timeStamp.setCellFactory(new Callback<TreeTableColumn, TreeTableCell>() {
            @Override
            public TreeTableCell call(TreeTableColumn param) {
                return new TreeTableCell<HDFDisplayTreeNode, HDFDisplayTreeNode>() {

                    @Override
                    protected void updateItem(HDFDisplayTreeNode item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item.getTimestamp());
                        }
                    }
                };
            }
        });

        treeTableView.getSelectionModel().setSelectionMode(
                SelectionMode.MULTIPLE
        );

        constructTree();


        rtTimePlot.setUpdateThrottle(200, TimeUnit.MILLISECONDS);

        rtTimePlot.getXAxis().setGridVisible(true);
        rtTimePlot.getXAxis().setAutoscale(true);

        plotArea.getChildren().add(rtTimePlot);
        // anchor to the button
        plotArea.setTopAnchor(rtTimePlot, 0.0);
        plotArea.setLeftAnchor(rtTimePlot, 0.0);
        plotArea.setRightAnchor(rtTimePlot, 0.0);
        plotArea.setBottomAnchor(rtTimePlot, 0.0);

    }

    @FXML
    public void createContextMenu(ContextMenuEvent e) {
        System.out.println("context....");
        final ObservableList<TreeItem<HDFDisplayTreeNode>> selectedItems = treeTableView.selectionModelProperty().getValue().getSelectedItems();

        contextMenu.getItems().clear();
        addPVs2Plot.setOnAction(event -> {
            selectedItems.stream().forEach(item -> {
                item.getValue().isPlotted().set(true);
            });
        });
        removePVsFromPlot.setOnAction(event -> {
            selectedItems.stream().forEach(item -> {
                item.getValue().isPlotted().set(false);
            });
        });
        contextMenu.getItems().add(addPVs2Plot);
        contextMenu.getItems().add(removePVsFromPlot);

        contextMenu.show(treeTableView.getScene().getWindow(), e.getScreenX(), e.getScreenY());


    }


    // A list of traces mapped to the associated selected pv's in the tree
    private final Map<String, Trace> traces = new ConcurrentHashMap<>();
    final RGBFactory colors = new RGBFactory();

    public synchronized void addToPlot(HDFDisplayTreeNode item) {
        if(!traces.containsKey(item.getName())){
            try {
                final ArrayPlotDataProvider<Instant> data = new ArrayPlotDataProvider<>();
                H5ScalarDS rawData = item.getData();
                rawData.open();

                float[] d1 = (float[]) rawData.getData();
                for (int i = 0; i < d1.length; i++) {
                    data.add(new SimpleDataItem<>(item.getInstant().plusMillis(i*1), d1[i]));
                }
                Trace<Instant> trace = rtTimePlot.addTrace(item.getName(), "socks", data, colors.next(), TraceType.LINES, 3, LineStyle.SOLID, PointType.NONE, 3, 0);
                traces.put(item.getName(), trace);
                rtTimePlot.getYAxes().get(trace.getYAxis()).setAutoscale(true);
                rtTimePlot.requestUpdate();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void remoteFromPlot(HDFDisplayTreeNode item) {
        if(traces.containsKey(item.getName())){
            rtTimePlot.removeTrace(traces.remove(item.getName()));
        }
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
