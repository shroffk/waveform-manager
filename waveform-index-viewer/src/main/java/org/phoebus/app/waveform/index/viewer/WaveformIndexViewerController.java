package org.phoebus.app.waveform.index.viewer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.phoebus.app.waveform.index.viewer.entity.WaveformIndex;
import org.w3c.dom.Text;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.phoebus.app.waveform.index.viewer.WaveformIndexViewerApp.logger;

public class WaveformIndexViewerController {

    @FXML
    TableView tableView;
    @FXML
    TableColumn name;
    @FXML
    TableColumn tags;


    @FXML
    TextField search;

    // Client resource
    private URI serviceURL;
    private volatile boolean initialized = false;
    private volatile WebResource service;
    private ObjectMapper mapper = new ObjectMapper();

    private final static String INFO = "info";

    // Model
    List<WaveformIndex> waveformIndices = Collections.emptyList();

    @FXML
    private void initialize() {
        serviceURL = URI.create(WaveformIndexViewerPreferences.waveform_index_url);
        if (!initialized) {
            Client client = Client.create(new DefaultClientConfig());
            client.setFollowRedirects(true);
            service = client.resource(serviceURL.toString());
        }

        // fetch the files
        String info = service.path(INFO)
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);

        logger.info("Successfully created a client to the waveform index service: " + info);

        // configure the table
        name.setCellValueFactory(new PropertyValueFactory<WaveformIndex, String>("file"));

        // configure the default search string
        search.setText("*");
        refresh();
    }

    public void refresh() {
        tableView.setItems(FXCollections.observableArrayList(this.waveformIndices));
    }

    @FXML
    public void query() {
        try {
            logger.info("Searching for waveform indices which match : " + search);
            this.waveformIndices = mapper.readValue(service.get(String.class), new TypeReference<List<WaveformIndex>>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
