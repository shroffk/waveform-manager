package org.phoebus.services.waveform.index;

import java.io.File;
import java.util.List;

import org.elasticsearch.client.RestHighLevelClient;
import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.phoebus.services.waveform.index.entity.WaveformFileAttribute;
import org.phoebus.services.waveform.index.entity.WaveformFilePVProperty;
import org.phoebus.services.waveform.index.entity.WaveformFileProperty;
import org.phoebus.services.waveform.index.entity.WaveformIndex;
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

    @Test
    public void createWaveformIndex() {
        File file = new File("test_file.h5");
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
        WaveformFileProperty fileProperty = new WaveformFileProperty();
        fileProperty.setName("propertyName");
        fileProperty.setAttributes(List.of(new WaveformFileAttribute("attribute1", "value"),
                new WaveformFileAttribute("attribute2", "value")));
        index.setProperties(List.of(fileProperty));
        createdIndex = waveformIndexRepository.save(index);

        Assert.assertThat(createdIndex, new CustomTypeSafeMatcher<WaveformIndex>("Expected Index to be created with property ") {
            @Override
            protected boolean matchesSafely(WaveformIndex item) {
                return item.getFile().equals(file.toURI())
                        && item.getProperties().contains(fileProperty);
            }
        });


        /*
         * Create a waveform index with pv properties
         */
        WaveformFilePVProperty filePvProperties = new WaveformFilePVProperty("sim://testPV");
        filePvProperties.setAttributes(List.of(new WaveformFileAttribute("pvAttribute1", "value"),
                                               new WaveformFileAttribute("pvAttribute2", "value")));
        index.setPvProperties(List.of(filePvProperties));

        createdIndex = waveformIndexRepository.save(index);

        Assert.assertThat(createdIndex, new CustomTypeSafeMatcher<WaveformIndex>("Expected Index to be created with pv property ") {
            @Override
            protected boolean matchesSafely(WaveformIndex item) {
                return item.getFile().equals(file.toURI())
                        && item.getPvProperties().contains(filePvProperties);
            }
        });

        waveformIndexRepository.delete(index);

    }

    @Test
    public void addTag() {

    }

    @Test
    public void addProperty() {

    }

    @Test
    public void addPvProperty() {

    }

}
