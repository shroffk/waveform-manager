package org.phoebus.services.waveform.index;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.phoebus.services.waveform.index.entity.WaveformFileAttribute;
import org.phoebus.services.waveform.index.entity.WaveformFilePVProperty;
import org.phoebus.services.waveform.index.entity.WaveformFileProperty;
import org.phoebus.services.waveform.index.entity.WaveformFileTag;
import org.phoebus.services.waveform.index.entity.WaveformIndex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Integration tests for the resource which exposes the user facing REST API.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = WaveformIndexService.class)
@WebAppConfiguration
public class WaveformIndexResourceIT {

    @Autowired
    private WaveformIndexRepository waveformIndexRepository;
    @Autowired
    private WaveformIndexResource waveformIndexResource;

    @Test
    public void createIndex() {
        try {
            WaveformIndex createdIndex = waveformIndexResource.createIndex(new WaveformIndex("testFile"));
            assertTrue("failed to retrieve index : testFile", createdIndex.equals(new WaveformIndex("testFile")));
        } finally {
            // Cleanup
            waveformIndexRepository.delete("testFile");
        }
    }

    @Test
    public void getIndex() {
        WaveformIndex createdIndex;
        try {
            createdIndex = waveformIndexRepository.save(new WaveformIndex("testFile"));
            // GET index
            WaveformIndex getIndex = waveformIndexResource.getIndex("testFile");
            assertTrue("failed to retrieve index : testFile", createdIndex.equals(getIndex));
        } finally {
            // Cleanup
            waveformIndexRepository.delete("testFile");
        }
    }

    @Test
    public void deleteIndex() {
        try {
            // Create a Index using the WaveformIndexRepository
            WaveformIndex createdIndex = waveformIndexRepository.save(new WaveformIndex("testFile"));
            // Delete resource using the WaveformIndexResource
            waveformIndexResource.deleteIndex(createdIndex.getFile().toString());
        } finally {
            // Cleanup
            waveformIndexRepository.delete("testFile");
        }
    }

    // Tests for WaveformFileTag operations

    @Test
    public void addTag2Index() {
        waveformIndexResource.addTag(basicWaveformIndexFile, "addTestTag");
        assertTrue("Failed to add a test tag 'addTestTag' to index : ",
                waveformIndexResource.getIndex(basicWaveformIndexFile).getTags().contains(new WaveformFileTag("addTestTag")));
    }

    @Test
    public void addTag2MissingIndex() {
        try {
            waveformIndexResource.addTag("missingFile", "testTag");
            fail("Expected an 404 to be thrown");
        } catch (ResponseStatusException e) {
            assertTrue(e.getStatus().value() == 404);
        }
    }

    // Tests for WaveformFileProperty operations

    @Test
    public void addProperty2Index() {
        // An empty property with no attributes
        WaveformFileProperty emptyWaveformFileProperty = new WaveformFileProperty("emptyProperty");
        waveformIndexResource.addProperty(basicWaveformIndexFile, emptyWaveformFileProperty);
        assertTrue("failed to add an empty property to index: ",
                waveformIndexResource.addProperty(basicWaveformIndexFile, emptyWaveformFileProperty).getProperties().contains(emptyWaveformFileProperty));

        // A property with 2 attributes
        WaveformFileProperty waveformFileProperty = new WaveformFileProperty("addTestProperty");
        waveformFileProperty.addAttribute(new WaveformFileAttribute("testAttribute1", "value1"));
        waveformFileProperty.addAttribute(new WaveformFileAttribute("testAttribute2", "value2"));
        assertTrue("failed to add a simple property to index: ",
                waveformIndexResource.addProperty(basicWaveformIndexFile, waveformFileProperty).getProperties()
                        .contains(waveformFileProperty));
    }

    @Test
    public void addProperty2MissingIndex() {
        try {
            waveformIndexResource.addProperty("missingFile", new WaveformFileProperty("emptyProperty"));
            fail("Expected an 404 to be thrown");
        } catch (ResponseStatusException e) {
            assertTrue(e.getStatus().value() == 404);
        }
    }

    // Tests for WaveformFilePVProperty operations

    @Test
    public void addPVProperty2Index() {
        // An empty property with no attributes
        WaveformFilePVProperty emptyWaveformFilePVProperty = new WaveformFilePVProperty("emptyPVProperty");
        assertTrue("failed to add an empty pvproperty to index: ",
                waveformIndexResource.addPvProperty(basicWaveformIndexFile, emptyWaveformFilePVProperty).getPvProperties()
                        .contains(emptyWaveformFilePVProperty));

        // A property with 2 attributes
        WaveformFilePVProperty waveformFilePVProperty = new WaveformFilePVProperty("addTestPVProperty");
        waveformFilePVProperty.addAttribute(new WaveformFileAttribute("testAttribute1", "value1"));
        waveformFilePVProperty.addAttribute(new WaveformFileAttribute("testAttribute2", "value2"));
        assertTrue("failed to add a simple pv property to index: ",
                waveformIndexResource.addPvProperty(basicWaveformIndexFile, emptyWaveformFilePVProperty).getPvProperties()
                        .contains(emptyWaveformFilePVProperty));


    }

    @Test
    public void addPvProperty2MissingIndex() {
        try {
            waveformIndexResource.addPvProperty("missingFile", new WaveformFilePVProperty("emptyPVProperty"));
            fail("Expected an 404 to be thrown");
        } catch (ResponseStatusException e) {
            assertTrue(e.getStatus().value() == 404);
        }
    }

    @Test
    public void deleteMissingIndex() {
        try {
            waveformIndexResource.deleteIndex("missingFile");
            fail("Expected an 404 to be thrown");
        } catch (ResponseStatusException e) {
            assertTrue(e.getStatus().value() == 404);
        }
    }

    private final String basicWaveformIndexFile = "file:///empty/file.h5";
    private WaveformIndex basicWaveformIndex = new WaveformIndex(basicWaveformIndexFile);

    @Before
    public void before() {
        // Create a repository instance for pre test setup
        waveformIndexRepository.save(basicWaveformIndex);
    }

    /**
     * cleanup all the entries created for testing
     */
    @After
    public void after() {
        waveformIndexRepository.delete(basicWaveformIndexFile);
    }
}
