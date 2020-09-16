package org.phoebus.app.waveform.index.viewer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import org.epics.waveform.index.util.entity.WaveformFileTag;
import org.epics.waveform.index.util.entity.WaveformIndex;
import org.phoebus.app.waveform.index.viewer.jobs.AddTag2WaveformIndex;
import org.phoebus.app.waveform.index.viewer.jobs.RemoveTag2WaveformIndex;
import org.phoebus.app.waveform.index.viewer.ui.RemoveTagDialog;
import org.phoebus.framework.jobs.Job;
import org.phoebus.framework.jobs.JobManager;
import org.phoebus.framework.jobs.JobMonitor;
import org.phoebus.framework.jobs.JobRunnable;
import org.phoebus.framework.spi.AppResourceDescriptor;
import org.phoebus.framework.util.ResourceParser;
import org.phoebus.framework.workbench.ApplicationService;
import org.phoebus.ui.application.Messages;
import org.phoebus.ui.application.PhoebusApplication;
import org.phoebus.ui.dialog.ExceptionDetailsErrorDialog;
import org.phoebus.ui.javafx.ImageCache;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.phoebus.app.waveform.index.viewer.WaveformIndexViewerApp.logger;

public class WaveformIndexViewerController {

    @FXML
    TableView<WaveformIndex> tableView;
    @FXML
    TableColumn name;
    @FXML
    TableColumn<WaveformIndex, WaveformIndex> tags;

    @FXML
    TextField search;

    // Client resource
    private URI serviceURL;
    private AtomicBoolean initialized = new AtomicBoolean(false);
    private volatile WebResource service;
    private ObjectMapper mapper = new ObjectMapper();

    private final static String INFO = "info";

    // Model
    List<WaveformIndex> waveformIndices = Collections.emptyList();

    // jobs
    private Job addPropertyJob;
    private Job addTagJob;
    private Job removePropertyJob;
    private Job removeTagJob;


    @FXML
    private void initialize() {
        serviceURL = URI.create(WaveformIndexViewerPreferences.waveform_index_url);
        JobRunnable initializeService = new JobRunnable() {
            @Override
            public void run(JobMonitor jobMonitor) throws Exception {
                if (!initialized.get()) {
                    Client client = Client.create(new DefaultClientConfig());
                    client.setFollowRedirects(true);
                    service = client.resource(serviceURL.toString());
                    logger.info("Successfully initialized the ");
                    initialized.set(true);
                }
            }
        };
        Job job = JobManager.schedule("initialize waveform Index : ", initializeService);

        // fetch the files
        String info = service.path(INFO)
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);

        logger.info("Successfully created a client to the waveform index service: " + info);

        // configure the table
        name.setCellValueFactory(new PropertyValueFactory<WaveformIndex, String>("file"));
        name.setMaxWidth(1f * Integer.MAX_VALUE * 75);

        tags.setMaxWidth(1f * Integer.MAX_VALUE * 25);
        tags.setCellValueFactory(col -> {
            return new SimpleObjectProperty(col.getValue());
        });
        tags.setCellFactory(col -> {
            final GridPane pane = new GridPane();
            final Label tags = new Label();
            pane.addColumn(0, tags);

            return new TableCell<WaveformIndex, WaveformIndex>() {

                @Override
                public void updateItem(WaveformIndex index, boolean empty) {
                    super.updateItem(index, empty);
                    if (empty) {
                        tags.setText("");
                        setGraphic(null);
                    } else {
                        tags.setText(index.getTags().stream().map(WaveformFileTag::getName)
                                .collect(Collectors.joining(System.lineSeparator())));
                        tags.setGraphic(new ImageView(addTags));
                        setGraphic(pane);
                    }
                }
            };
        });
        // configure the default search string
        search.setText("file=*");
        refresh();
    }

    public void refresh() {
        tableView.setItems(FXCollections.observableArrayList(this.waveformIndices));
    }

    @FXML
    public void query() {
        try {
            logger.info("Searching for waveform indices which match : " + search.getText());
            this.waveformIndices = mapper.readValue(
                    service.path("search").queryParams(buildSearchMap(search.getText())).get(String.class),
                    new TypeReference<List<WaveformIndex>>() {
            });
            refresh();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    Image addProperties = ImageCache.getImage(WaveformIndexViewerApp.class, "/icons/add_properties.png");
    Image addTags = ImageCache.getImage(WaveformIndexViewerApp.class, "/icons/add_tag.png");
    Image removeProperties = ImageCache.getImage(WaveformIndexViewerApp.class, "/icons/remove_properties.png");
    Image removeTags = ImageCache.getImage(WaveformIndexViewerApp.class, "/icons/remove_tag.png");
    Image openURI = ImageCache.getImage(PhoebusApplication.class, "/icons/fldr_obj.png");

    final ContextMenu contextMenu = new ContextMenu();
    private final Menu openWith = new Menu(Messages.OpenWith, ImageCache.getImageView(WaveformIndexViewerApp.class, "/icons/fldr_obj.png"));

    @FXML
    public void createContextMenu() {
        contextMenu.getItems().clear();

        List<URI> fileURIs = tableView.getSelectionModel().getSelectedItems().stream().map(index -> {
            return index.getFile();
        }).collect(Collectors.toList());

        if(fileURIs.size() == 1) {
            URI resource = fileURIs.get(0);
            final List<AppResourceDescriptor> applications = ApplicationService.getApplications(resource);
            if (applications.size() > 0)
            {
                MenuItem open = new MenuItem("Open", new ImageView(openURI));
                openWith.getItems().clear();
                for (AppResourceDescriptor app : applications)
                {
                    final MenuItem open_app = new MenuItem(app.getDisplayName());
                    final URL icon_url = app.getIconURL();
                    if (icon_url != null)
                        open_app.setGraphic(new ImageView(icon_url.toExternalForm()));
                    open_app.setOnAction(event -> app.create(resource));
                    openWith.getItems().add(open_app);
                }
                contextMenu.getItems().add(openWith);
                contextMenu.getItems().add(new SeparatorMenuItem());
            }
        }


        // Add tag to waveform file
        MenuItem addTag = new MenuItem("Add tag", new ImageView(addTags));
        addTag.setOnAction(e -> {

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Tag");
            dialog.setHeaderText("Add Tag");
            dialog.setGraphic(new ImageView(addTags));
            dialog.setContentText("Enter Tag Name: ");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(tagName -> {
                if (addTagJob != null) {
                    addTagJob.cancel();
                }
                List<String> fileNames = tableView.getSelectionModel().getSelectedItems().stream().map(index -> {
                    return index.getFile().toString();
                }).collect(Collectors.toList());

                addTagJob = AddTag2WaveformIndex.submit(service,
                        fileNames,
                        tagName,
                        (url, ex) -> ExceptionDetailsErrorDialog.openError("Add tag : " + tagName + " to WaveformIndex Error", ex.getMessage(), ex));
            });
        });
        // Remove tag to waveform file
        MenuItem removeTag = new MenuItem("Remove tag", new ImageView(removeTags));
        removeTag.setOnAction(e -> {

            List<String> fileNames = new ArrayList<>();
            Set<String> tags = new HashSet<>();

            tableView.getSelectionModel().getSelectedItems().stream().forEach(index -> {
                fileNames.add(index.getFile().toString());
                tags.addAll(index.getTags().stream().map(WaveformFileTag::getName).collect(Collectors.toSet()));
            });

            // get the list of cf properties
            RemoveTagDialog dialog = new RemoveTagDialog(tableView, tags);
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(tagName -> {
                if (removeTagJob != null) {
                    removeTagJob.cancel();
                }
                removeTagJob = RemoveTag2WaveformIndex.submit(
                        service,
                        fileNames,
                        tagName,
                        (url, ex) -> ExceptionDetailsErrorDialog.openError("Remove tag : " + tagName + "to WaveformIndex Error", ex.getMessage(), ex));

            });
        });


        // Remove Property to waveform file
        MenuItem addProperty = new MenuItem("Add Property", new ImageView(addProperties));
        // Remove Property to waveform file
        MenuItem removeProperty = new MenuItem("Remove Property", new ImageView(removeProperties));

        contextMenu.getItems().add(addTag);
        contextMenu.getItems().add(removeTag);
        contextMenu.getItems().add(addProperty);
        contextMenu.getItems().add(removeProperty);

        tableView.setContextMenu(contextMenu);
    }

    private static MultivaluedMap<String, String> buildSearchMap(String searchPattern) {
        MultivaluedMap<String, String> map = new MultivaluedMapImpl();
        searchPattern = searchPattern.replaceAll(", ", ",");
        Arrays.stream(searchPattern.split("\\s")).forEach(searchCriteria -> {
            if (searchCriteria.contains("=")) {
                String[] keyValue = searchCriteria.split("=");
                String key = keyValue[0];
                String valuePattern = keyValue[1];
                switch (key.toLowerCase()) {
                    case "file":
                    case "files":
                        map.add(key,valuePattern.trim());
                        break;
                    case "tag":
                    case "tags":
                        map.add(key,valuePattern.trim());
                        break;
                }
            } else {
                // TODO failed to parse search
            }
        });
        return map;
    }
}
