package org.phoebus.hdf.display;

import hdf.object.h5.H5ScalarDS;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
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

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

                                item.isPlotted().addListener((observable, oldValue, newValue) -> {
                                    addToPlot(item);
                                });
                                setGraphic(checkBox);
                            }
                        }
                    }
                };
            }
        });
        constructTree();


        rtTimePlot.setUpdateThrottle(200, TimeUnit.MILLISECONDS);

        rtTimePlot.getXAxis().setGridVisible(true);

        plotArea.getChildren().add(rtTimePlot);
        // anchor to the button
        plotArea.setTopAnchor(rtTimePlot, 0.0);
        plotArea.setLeftAnchor(rtTimePlot, 0.0);
        plotArea.setRightAnchor(rtTimePlot, 0.0);
        plotArea.setBottomAnchor(rtTimePlot, 0.0);

    }

    // A list of traces mapped to the associated selected pv's in the tree
    private final Map<String, Trace> traces = new HashMap<>();
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
                traces.put(item.getName(),
                        rtTimePlot.addTrace(item.getName(), "socks", data, colors.next(), TraceType.LINES, 3, LineStyle.SOLID, PointType.NONE, 3, 0));
                rtTimePlot.requestUpdate();

            } catch (Exception e) {
                e.printStackTrace();
            }
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
