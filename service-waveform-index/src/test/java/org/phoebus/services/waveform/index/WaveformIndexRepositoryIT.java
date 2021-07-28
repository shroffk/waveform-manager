package org.phoebus.services.waveform.index;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.RestHighLevelClient;
import org.epics.waveform.index.util.entity.Event;
import org.epics.waveform.index.util.entity.WaveformFileAttribute;
import org.epics.waveform.index.util.entity.WaveformFilePVProperty;
import org.epics.waveform.index.util.entity.WaveformFileProperty;
import org.epics.waveform.index.util.entity.WaveformFileTag;
import org.epics.waveform.index.util.entity.WaveformIndex;
import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ElasticConfig.class)
@TestPropertySource(locations = "classpath:test_application.properties")
public class WaveformIndexRepositoryIT {

    @Autowired
    @Qualifier("indexClient")
    RestHighLevelClient client;

    @Autowired
    private WaveformIndexRepository waveformIndexRepository;

    private static ClassLoader classLoader = WaveformIndexRepositoryIT.class.getClassLoader();
    private static File file = new File(classLoader.getResource("test_file.h5").getFile());

    @Test
    public void createWaveformIndex() throws IOException {
//        File file = new File("test_file.h5");
        WaveformIndex index = new WaveformIndex(file.toURI());
        // Create a simple waveform index with only a file
        WaveformIndex createdIndex = waveformIndexRepository.save(index);

        Assert.assertThat(createdIndex, new CustomTypeSafeMatcher<WaveformIndex>("Check File Only") {
            @Override
            protected boolean matchesSafely(WaveformIndex item) {
                return item.getFile().equals(file.toURI());
            }
        });

        /*
         * Create a waveform index with a property
         */
        WaveformFileProperty fileProperty = new WaveformFileProperty("propertyName");
        fileProperty.setAttributes(Arrays.asList(new WaveformFileAttribute("attribute1", "value"),
                                                 new WaveformFileAttribute("attribute2", "value")));
        List<WaveformFileProperty> properties =  new ArrayList<>();
        properties.add(fileProperty);
        index.setProperties(properties);
        createdIndex = waveformIndexRepository.save(index);

        Assert.assertThat(createdIndex, new CustomTypeSafeMatcher<WaveformIndex>("Expected Index to be created with property ") {
            @Override
            protected boolean matchesSafely(WaveformIndex item) {
                return item.getFile().equals(file.toURI())
                        && item.getProperties().contains(fileProperty);
            }
        });

        /*
         * Create a waveform index with a tag
         */
        WaveformFileTag fileTag = new WaveformFileTag("tag");
        index.setTags(Arrays.asList(fileTag));
        createdIndex = waveformIndexRepository.save(index);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(System.out, createdIndex);
        Assert.assertThat(createdIndex, new CustomTypeSafeMatcher<WaveformIndex>("Expected Index to be created with tag ") {
            @Override
            protected boolean matchesSafely(WaveformIndex item) {
                return item.getFile().equals(file.toURI())
                        && item.getTags().contains(fileTag);
            }
        });

        /*
         * Create a waveform index with pv properties
         */
        WaveformFilePVProperty filePvProperties = new WaveformFilePVProperty("sim://testPV");
        filePvProperties.setAttributes(new HashSet<>() {{ add(new WaveformFileAttribute("pvAttribute1", "value"));
                                                          add(new WaveformFileAttribute("pvAttribute2", "value")); }});
        index.setPvProperties(Arrays.asList(filePvProperties));

        createdIndex = waveformIndexRepository.save(index);

        Assert.assertThat(createdIndex, new CustomTypeSafeMatcher<WaveformIndex>("Expected Index to be created with pv property ") {
            @Override
            protected boolean matchesSafely(WaveformIndex item) {
                return item.getFile().equals(file.toURI())
                        && item.getPvProperties().contains(filePvProperties);
            }
        });

        /*
         * Create a waveform index with events
         */
        Event start = new Event("start", Instant.EPOCH);
        Event end = new Event("end", Instant.now());
        index.setEvents(Arrays.asList(start, end));

        createdIndex = waveformIndexRepository.save(index);

        Assert.assertThat(createdIndex, new CustomTypeSafeMatcher<WaveformIndex>("Expected Index to be created with events ") {
            @Override
            protected boolean matchesSafely(WaveformIndex item) {
                return item.getFile().equals(file.toURI())
                        && item.getEvents().contains(start);
            }
        });
        waveformIndexRepository.delete(index);

    }

    @Test
    public void addTag() {
        File file = new File("test_file.h5");
        WaveformIndex index = new WaveformIndex(file.toURI());
        WaveformIndex createdIndex = waveformIndexRepository.save(index);

        WaveformIndex updatedIndex = waveformIndexRepository.addTag(createdIndex, new WaveformFileTag("addTestTag"));
        Assert.assertThat(updatedIndex, new CustomTypeSafeMatcher<WaveformIndex>("Expected Index to be updated with testTag ") {
            @Override
            protected boolean matchesSafely(WaveformIndex item) {
                return item.getFile().equals(file.toURI())
                        && item.getTags().contains(new WaveformFileTag("addTestTag"));
            }
        });
        waveformIndexRepository.delete(index);
    }

    @Test
    public void addProperty() {
        WaveformIndex index = new WaveformIndex(file.toURI());
        WaveformIndex createdIndex = waveformIndexRepository.save(index);

        WaveformFileProperty fileProperty = new WaveformFileProperty("addPropertyName");
        fileProperty.setAttributes(Arrays.asList(new WaveformFileAttribute("attribute1", "value"),
                                                 new WaveformFileAttribute("attribute2", "value")));
        WaveformIndex updatedIndex = waveformIndexRepository.addProperty(createdIndex, fileProperty);
        Assert.assertThat(updatedIndex, new CustomTypeSafeMatcher<WaveformIndex>("Expected Index to be created with property ") {
            @Override
            protected boolean matchesSafely(WaveformIndex item) {
                return item.getFile().equals(file.toURI())
                        && item.getProperties().contains(fileProperty);
            }
        });
        waveformIndexRepository.delete(index);
    }

    @Test
    public void addPvProperty() {
        File file = new File("test_file.h5");
        WaveformIndex index = new WaveformIndex(file.toURI());
        WaveformIndex createdIndex = waveformIndexRepository.save(index);

        WaveformFilePVProperty filePvProperties = new WaveformFilePVProperty("sim://testPV");
        filePvProperties.setAttributes(new HashSet<>() {{add(new WaveformFileAttribute("pvAttribute1", "value"));
                                                         add(new WaveformFileAttribute("pvAttribute2", "value"));}});
        WaveformIndex updatedIndex = waveformIndexRepository.addPvProperty(createdIndex, filePvProperties);
        Assert.assertThat(updatedIndex, new CustomTypeSafeMatcher<WaveformIndex>("Expected Index to be created with property ") {
            @Override
            protected boolean matchesSafely(WaveformIndex item) {
                return item.getFile().equals(file.toURI())
                        && item.getPvProperties().contains(filePvProperties);
            }
        });

        filePvProperties.addAttribute(new WaveformFileAttribute("pvAttribute3", "value"));
        updatedIndex = waveformIndexRepository.addPvProperty(createdIndex, filePvProperties);
        Assert.assertThat(updatedIndex, new CustomTypeSafeMatcher<WaveformIndex>("Expected Index to be created with property ") {
            @Override
            protected boolean matchesSafely(WaveformIndex item) {
                return item.getFile().equals(file.toURI())
                        && item.getPvProperties().contains(filePvProperties);
            }
        });
        waveformIndexRepository.delete(index);
    }

}
