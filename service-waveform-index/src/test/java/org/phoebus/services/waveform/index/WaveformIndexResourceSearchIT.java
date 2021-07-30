package org.phoebus.services.waveform.index;

import org.elasticsearch.client.RestHighLevelClient;
import org.epics.waveform.index.util.entity.Event;
import org.epics.waveform.index.util.entity.WaveformFileAttribute;
import org.epics.waveform.index.util.entity.WaveformFileProperty;
import org.epics.waveform.index.util.entity.WaveformFileTag;
import org.epics.waveform.index.util.entity.WaveformIndex;
import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration tests for the resource which exposes the user facing REST API.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = WaveformIndexService.class)
@TestExecutionListeners(listeners = {WaveformIndexResourceSearchIT.class})
@TestPropertySource(locations = "classpath:test_application.properties")
@WebAppConfiguration
public class WaveformIndexResourceSearchIT implements TestExecutionListener {

    @Autowired
    @Qualifier("indexClient")
    RestHighLevelClient client;

    private static WaveformIndexRepository waveformIndexRepository;
    private static WaveformIndexResource waveformIndexResource;

    private static WaveformIndex index1;
    private static WaveformIndex createdIndex1;
    private static WaveformIndex index2;
    private static WaveformIndex createdIndex2;

    private static ClassLoader classLoader = WaveformIndexRepositoryIT.class.getClassLoader();
    private static File file1 = new File(classLoader.getResource("test_file.h5").getFile());
    private static File file2 = new File(classLoader.getResource("test_file_2.h5").getFile());

    // Tags
    private static WaveformFileTag fileTag1 = new WaveformFileTag("tag_1");
    private static WaveformFileTag fileTag2 = new WaveformFileTag("tag_2");

    // Events
    private static Event event_hour_ago = new Event("testEvent_1", Instant.now().minusSeconds(3600));
    private static Event event_2hours_ago = new Event("testEvent_2", Instant.now().minusSeconds(2*3600));

    // Properties
    private static WaveformFileAttribute fileAttribute1 = new WaveformFileAttribute("testAttribute1", "testValue_1");
    private static WaveformFileAttribute fileAttribute2 = new WaveformFileAttribute("testAttribute2", "testValue_2");

    private static WaveformFileProperty fileProperty1 = new WaveformFileProperty("testProperty1");
    private static WaveformFileProperty fileProperty2 = new WaveformFileProperty("testProperty2");
    static {
        fileAttribute1.setValue("file_1");
        fileProperty1.addAttribute(fileAttribute1);

        fileAttribute2.setValue("file_2");
        fileProperty2.addAttribute(fileAttribute2);
    }

    @Override
    public void beforeTestClass(TestContext testContext) throws InterruptedException {

        waveformIndexRepository = (WaveformIndexRepository) testContext.getApplicationContext().getBean("waveformIndexRepository");
        waveformIndexResource = (WaveformIndexResource) testContext.getApplicationContext().getBean("waveformIndexResource");

        index1 = new WaveformIndex(file1.toURI());
        index1.setProperties(Arrays.asList(fileProperty1));
        index1.setTags(Arrays.asList(fileTag1));
        // Create a simple waveform index with only a file
        createdIndex1 = waveformIndexRepository.save(index1);

        index2 = new WaveformIndex(file2.toURI());
        index2.setProperties(Arrays.asList(fileProperty2));
        index2.setTags(Arrays.asList(fileTag2));
        // Create a simple waveform index with only a file
        createdIndex2 = waveformIndexRepository.save(index2);
    }

    @Override
    public void afterTestClass(TestContext testContext) throws IOException {
        if( createdIndex1 != null) {
            waveformIndexRepository.delete(createdIndex1);
        }
        if (createdIndex2 != null) {
            waveformIndexRepository.delete(createdIndex2);
        }
    }

    @Test
    public void searchByFileName() {
        MultiValueMap<String, String> search = new LinkedMultiValueMap<String, String>();
        search.add("file", "*test_file.h5");
        List<WaveformIndex> result = waveformIndexResource.findIndices(search);
        assertThat("Failed to search for based on file name",
                result.size()==1 && result.contains(createdIndex1));

        search.clear();
        search.add("file", "*test_file_2.h5");
        result = waveformIndexResource.findIndices(search);
        assertThat("Failed to search for based on file name",
                result.size()==1 && result.contains(createdIndex2));
    }

    @Test
    public void searchByFileTags() {
        MultiValueMap<String, String> search = new LinkedMultiValueMap<String, String>();
        search.add("tag", "tag_1");
        List<WaveformIndex> result = waveformIndexResource.findIndices(search);
        assertThat("Failed to search for based on tag name",
                result.size()==1 && result.contains(createdIndex1));

        search.clear();
        search.add("tag", "tag_*");
        result = waveformIndexResource.findIndices(search);
        assertThat("Failed to search for based on tag name with wildcards",
                result.size() == 2 && result.contains(createdIndex1) && result.contains(createdIndex2));
    }

    @Test
    public void searchByFileProperties() {

        MultiValueMap<String, String> search = new LinkedMultiValueMap<String, String>();
        search.add("properties", "testProperty1");
        List<WaveformIndex> result = waveformIndexResource.findIndices(search);
        assertThat("Failed to search for based on property name",
                result.size()==1 && result.contains(createdIndex1));

        search.clear();
        search.add("properties", "testProperty1.testAttribute1");
        result = waveformIndexResource.findIndices(search);
        assertThat("Failed to search for based on property and attribute names",
                result.size()==1 && result.contains(createdIndex1));

        search.clear();
        search.add("properties", "testProperty1.testAttribute1.file_1");
        result = waveformIndexResource.findIndices(search);
        assertThat("Failed to search for based on property, attribute and value",
                result.size()==1 && result.contains(createdIndex1));

        search.clear();
        search.add("properties", "testProperty*.testAttribute*.file_*");
        result = waveformIndexResource.findIndices(search);
        assertThat("Failed to search for based on property, attribute and value with wildcards",
                result.size() == 2 && result.contains(createdIndex1) && result.contains(createdIndex2));
    }

    public void searchByEvents() {

    }
}
