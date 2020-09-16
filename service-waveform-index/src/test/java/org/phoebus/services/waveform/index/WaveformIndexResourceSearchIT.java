package org.phoebus.services.waveform.index;

import org.elasticsearch.client.RestHighLevelClient;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Integration tests for the resource which exposes the user facing REST API.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = WaveformIndexService.class)
@WebAppConfiguration
public class WaveformIndexResourceSearchIT {

    @Autowired
    @Qualifier("indexClient")
    RestHighLevelClient client;

    @Autowired
    private WaveformIndexRepository waveformIndexRepository;
    @Autowired
    private WaveformIndexResource waveformIndexResource;

}
