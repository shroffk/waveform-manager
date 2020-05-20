package org.phoebus.services.waveform.index;

import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.phoebus.services.waveform.index.entity.WaveformFilePVProperty;
import org.phoebus.services.waveform.index.entity.WaveformFileProperty;
import org.phoebus.services.waveform.index.entity.WaveformIndex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.*;

/**
 * Integration tests for the resource which exposes the user facing REST API.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = WaveformIndexService.class)
@WebAppConfiguration
public class WaveformIndexResourceIT {

    @Autowired
    @Qualifier("indexClient")
    RestHighLevelClient client;

    @Autowired
    private WaveformIndexRepository waveformIndexRepository;
    @Autowired
    private WaveformIndexResource waveformIndexResource;

    @Test
    public void createIndex() {

    }

    @Test
    public void getIndex() {
        WaveformIndex createdIndex = waveformIndexRepository.save(new WaveformIndex("testFile"));

        // GET index
        WaveformIndex getIndex = waveformIndexResource.getIndex("testFile");
        assertTrue("failed to retrieve index : testFile", createdIndex.equals(getIndex));
    }

    @Test
    public void deleteIndex(){

    }

    @Test
    public void addTag2MissingIndex(){
        try {
            waveformIndexResource.addTag("missingFile", "testTag");
            fail("Expected an 404 to be thrown");
        } catch (ResponseStatusException e) {
            assertTrue(e.getStatus().value() == 404);
        }
    }

    @Test
    public void addProperty2MissingIndex(){
        try {
            waveformIndexResource.addProperty("missingFile", new WaveformFileProperty("emptyProperty"));
            fail("Expected an 404 to be thrown");
        } catch (ResponseStatusException e) {
            assertTrue(e.getStatus().value() == 404);
        }
    }

    @Test
    public void addPvProperty2MissingIndex(){
        try {
            waveformIndexResource.addPvProperty("missingFile", new WaveformFilePVProperty("emptyPVProperty"));
            fail("Expected an 404 to be thrown");
        } catch (ResponseStatusException e) {
            assertTrue(e.getStatus().value() == 404);
        }
    }

    @Test
    public void deleteMissingIndex(){
        try {
            waveformIndexResource.deleteIndex("missingFile");
            fail("Expected an 404 to be thrown");
        } catch (ResponseStatusException e) {
            assertTrue(e.getStatus().value() == 404);
        }
    }
}
