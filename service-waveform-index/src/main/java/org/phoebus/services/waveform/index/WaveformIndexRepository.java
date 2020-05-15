package org.phoebus.services.waveform.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentType;
import org.phoebus.services.waveform.index.entity.WaveformIndex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.elasticsearch.action.DocWriteResponse.Result;

import java.util.Optional;
import java.util.logging.Level;

import static org.phoebus.services.waveform.index.WaveformIndexService.*;
@Repository
public class WaveformIndexRepository {

    // Read the elatic index and type from the application.properties
    @Value("${elasticsearch.wf.index:waveform_index}")
    private String ES_WF_INDEX;
    @Value("${elasticsearch.wf.type:waveform_index}")
    private String ES_WF_TYPE;

    @Autowired
    @Qualifier("indexClient")
    RestHighLevelClient client;

    private static final ObjectMapper mapper = new ObjectMapper();

    public <S extends WaveformIndex> S save(S entity) {
        try {
            IndexRequest indexRequest = new IndexRequest(ES_WF_INDEX, ES_WF_TYPE, entity.getFile().toString())
                                                    .source(mapper.writeValueAsBytes(entity), XContentType.JSON);
            IndexResponse response = client.index(indexRequest, RequestOptions.DEFAULT);
            if (response.getResult().equals(Result.CREATED)) {
                BytesReference ref = client.get(new GetRequest(ES_WF_INDEX, ES_WF_TYPE, response.getId()),
                                                RequestOptions.DEFAULT).getSourceAsBytesRef();
                WaveformIndex createdWaveformIndex = mapper.readValue(ref.streamInput(), WaveformIndex.class);
                return (S) createdWaveformIndex;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    public void delete(WaveformIndex waveformIndex) {
    }
}
