package org.phoebus.services.waveform.index;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.phoebus.services.waveform.index.ElasticConfig;
import org.phoebus.services.waveform.index.entity.WaveformFileAttribute;
import org.phoebus.services.waveform.index.entity.WaveformFileProperties;
import org.phoebus.services.waveform.index.entity.WaveformIndex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ElasticConfig.class)
@TestPropertySource(locations="classpath:test_application.properties")
public class WaveformIndexRepositoryIT
{

    @Autowired
    @Qualifier("indexClient")
    RestHighLevelClient client;

    @Autowired
    private WaveformIndexRepository waveformIndexRepository;

    @Test
    public void saveWaveformIndex() {
        File file = new File("test_file.h5");
        WaveformIndex index = new WaveformIndex(file.toURI());
        // Create a simple waveform index with only a file
        WaveformIndex createdIndex = waveformIndexRepository.save(index);

        WaveformFileProperties fileProperties = new WaveformFileProperties();
        fileProperties.setName("propertyName");
        fileProperties.setAttributes(List.of(new WaveformFileAttribute("attribute1", "value"),
                                             new WaveformFileAttribute("attribute2", "value")));
        index.setProperties(List.of(fileProperties));

        // Create a waveform index with a property
        createdIndex = waveformIndexRepository.save(index);


        // Create a waveform index with pv properties
        createdIndex = waveformIndexRepository.save(index);
    }

    @Test
    public void deleteWaveformIndex() {

    }

}
