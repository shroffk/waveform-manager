package org.phoebus.hdf.display;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import hdf.object.h5.H5ScalarDS;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import org.csstudio.javafx.rtplot.LineStyle;
import org.csstudio.javafx.rtplot.PointType;
import org.csstudio.javafx.rtplot.RTTimePlot;
import org.csstudio.javafx.rtplot.Trace;
import org.csstudio.javafx.rtplot.TraceType;
import org.csstudio.javafx.rtplot.data.ArrayPlotDataProvider;
import org.csstudio.javafx.rtplot.data.SimpleDataItem;
import org.csstudio.javafx.rtplot.util.RGBFactory;
import org.phoebus.app.waveform.index.viewer.WaveformIndexViewerPreferences;
import org.phoebus.app.waveform.index.viewer.entity.WaveformFileAttribute;
import org.phoebus.app.waveform.index.viewer.entity.WaveformFilePVProperty;
import org.phoebus.app.waveform.index.viewer.entity.WaveformIndex;
import org.phoebus.core.types.ProcessVariable;
import org.phoebus.framework.adapter.AdapterService;
import org.phoebus.framework.jobs.Job;
import org.phoebus.framework.jobs.JobManager;
import org.phoebus.framework.jobs.JobMonitor;
import org.phoebus.framework.jobs.JobRunnable;
import org.phoebus.framework.selection.SelectionService;
import org.phoebus.ui.application.ContextMenuService;
import org.phoebus.ui.application.PhoebusApplication;
import org.phoebus.ui.javafx.ImageCache;
import org.phoebus.ui.spi.ContextMenuEntry;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static org.phoebus.hdf.display.HDFFileProcessor.HDFDisplayTreeNode;
import static org.phoebus.hdf.display.HDFDisplayApp.logger;

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
    private final ContextMenu tableContextMenu = new ContextMenu();
    private final ContextMenu plotContextMenu = new ContextMenu();

    // HDF file
    private File file;
    // WaveformIndex
    // Client resource
    private URI serviceURL;
    private AtomicBoolean initialized = new AtomicBoolean(false);
    private volatile WebResource service;

    private WaveformIndex waveformIndex;

    @FXML
    public void initialize() {

        serviceURL = URI.create(WaveformIndexViewerPreferences.waveform_index_url);
        JobRunnable initializeService = new JobRunnable() {
            @Override
            public void run(JobMonitor jobMonitor) throws Exception {
                if (!initialized.get()) {
                    Client client = Client.create(new DefaultClientConfig());
                    client.setFollowRedirects(true);
                    service = client.resource(serviceURL.toString());
                    initialized.set(true);
                }
            }
        };
        Job job = JobManager.schedule("initialize waveform Index : ", initializeService);

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
                            if (item.isLeaf()) {
                                CheckBox checkBox = new CheckBox();
                                SimpleBooleanProperty plottingProperty = item.isPlotted();

                                checkBox.selectedProperty().bindBidirectional(plottingProperty);
                                checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                                    if (newValue.booleanValue() && !oldValue.booleanValue()) {
                                        addToPlot(item);
                                    } else if (!newValue.booleanValue() && oldValue.booleanValue()) {
                                        removeFromPlot(item);
                                    }
                                });
                                if (plottingProperty.get()) {
                                    addToPlot(item);
                                }
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
        final ObservableList<TreeItem<HDFDisplayTreeNode>> selectedItems = treeTableView.selectionModelProperty().getValue().getSelectedItems();

        tableContextMenu.getItems().clear();
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
        tableContextMenu.getItems().add(addPVs2Plot);
        tableContextMenu.getItems().add(removePVsFromPlot);

        tableContextMenu.show(treeTableView.getScene().getWindow(), e.getScreenX(), e.getScreenY());
    }

    @FXML
    public void createPlotContextMenu(ContextMenuEvent e) {
        plotContextMenu.getItems().clear();
        SelectionService.getInstance().setSelection(rtTimePlot, traces.keySet().stream().map((trace) ->
        {
            return new ProcessVariable(trace);
        }).collect(Collectors.toList()));

        List<ContextMenuEntry> contextEntries = ContextMenuService.getInstance().listSupportedContextMenuEntries();
        contextEntries.forEach(entry -> {
            MenuItem item = new MenuItem(entry.getName(), new ImageView(entry.getIcon()));
            item.setOnAction(event -> {
                try {
                    entry.callWithSelection(SelectionService.getInstance().getSelection());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            });
            plotContextMenu.getItems().add(item);
        });

        plotContextMenu.show(rtTimePlot.getScene().getWindow(), e.getScreenX(), e.getScreenY());

    }

    // A list of traces mapped to the associated selected pv's in the tree
    private final Map<String, Trace> traces = new ConcurrentHashMap<>();
    final RGBFactory colors = new RGBFactory();

    private final WaveformFileAttribute waveformFileAttributeTrue = new WaveformFileAttribute("plot", "true");
    private final WaveformFileAttribute waveformFileAttributeFalse = new WaveformFileAttribute("plot", "false");

    public synchronized void addToPlot(HDFDisplayTreeNode item) {

        if (!traces.containsKey(item.getName())) {
            try {
                final ArrayPlotDataProvider<Instant> data = new ArrayPlotDataProvider<>();
                H5ScalarDS rawData = item.getData();
                rawData.open();

                float[] d1 = (float[]) rawData.getData();
                for (int i = 0; i < d1.length; i++) {
                    data.add(new SimpleDataItem<>(item.getInstant().plusMillis(i * 1), d1[i]));
                }
                Trace<Instant> trace = rtTimePlot.addTrace(item.getName(), "socks", data, colors.next(), TraceType.LINES, 3, LineStyle.SOLID, PointType.NONE, 3, 0);
                traces.put(item.getName(), trace);
                rtTimePlot.getYAxes().get(trace.getYAxis()).setAutoscale(true);
                rtTimePlot.requestUpdate();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (waveformIndex != null) {
            WaveformFilePVProperty pvProperty = new WaveformFilePVProperty(item.getName());
            pvProperty.addAttribute(new WaveformFileAttribute("plot", "true"));
            if (!waveformIndex.getPvProperties().contains(pvProperty)) {
                JobRunnable addPVProperty = new JobRunnable() {
                    @Override
                    public void run(JobMonitor jobMonitor) throws Exception {
                        try {
                            service.path("add/pvproperties")
                                    .queryParam("fileURI", file.toURI().toString())
                                    .type(MediaType.APPLICATION_JSON)
                                    .post(mapper.writeValueAsString(pvProperty));
                            retrieveIndex();
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }
                };
                JobManager.schedule("add " + item.getName() + " pv property to waveform Index : ", addPVProperty);
            }
        }
    }

    private void removeFromPlot(HDFDisplayTreeNode item) {
        if (traces.containsKey(item.getName())) {
            rtTimePlot.removeTrace(traces.remove(item.getName()));
        }

        if (waveformIndex != null) {
            Set<WaveformFilePVProperty> foundProperty = waveformIndex.getPvProperties().stream()
                    .filter((pvProperty) -> {
                        return pvProperty.getPvName().equalsIgnoreCase(item.getName()) &&
                                pvProperty.getAttributes().contains(waveformFileAttributeTrue);
                    }).collect(Collectors.toSet());

            if (!foundProperty.isEmpty()) {
                JobRunnable removePVProperty = new JobRunnable() {
                    @Override
                    public void run(JobMonitor jobMonitor) throws Exception {
                        try {
                            WaveformFilePVProperty pvProperty = new WaveformFilePVProperty(item.getName());
                            pvProperty.addAttribute(new WaveformFileAttribute("plot", "false"));
                            service.path("add/pvproperties")
                                    .queryParam("fileURI", file.toURI().toString())
                                    .type(MediaType.APPLICATION_JSON)
                                    .post(mapper.writeValueAsString(pvProperty));
                            retrieveIndex();
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }
                };
                JobManager.schedule("remove " + item.getName() + " pv property waveform Index : ", removePVProperty);
            }
        }
    }


    public void setFile(File file) {
        this.file = file;
        retrieveIndex();
        constructTree();
    }

    public void constructTree() {
        try {
            TreeItem root = HDFFileProcessor.processFile(this.file, this.waveformIndex);
            treeTableView.setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ObjectMapper mapper = new ObjectMapper();

    public void retrieveIndex() {
        if (service != null) {
            try {
                this.waveformIndex = mapper.readValue(
                        service.queryParam("fileURI", URLEncoder.encode(this.file.toURI().toString(), "UTF-8")).get(String.class),
                        WaveformIndex.class);
            } catch (Exception e) {
                logger.log(Level.WARNING, "failed to retrieve the index info from the waveform file index service." + e.getMessage(), e);
            }
        }
    }

}
