package org.phoebus.services.waveform.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.engine.DocumentMissingException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.epics.waveform.index.util.entity.WaveformFilePVProperty;
import org.epics.waveform.index.util.entity.WaveformFileProperty;
import org.epics.waveform.index.util.entity.WaveformFileTag;
import org.epics.waveform.index.util.entity.WaveformIndex;
import org.epics.waveform.index.util.spi.ProcessWaveformIndex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static org.phoebus.services.waveform.index.WaveformIndexService.*;

import static org.elasticsearch.index.query.QueryBuilders.disMaxQuery;
import static org.elasticsearch.index.query.QueryBuilders.nestedQuery;
import static org.elasticsearch.index.query.QueryBuilders.wildcardQuery;

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


    private static ServiceLoader<ProcessWaveformIndex> loader;
    static {
        loader = ServiceLoader.load(ProcessWaveformIndex.class);
    }
    /**
     * Find the waveformIndex identified by fileURI
     * @param fileURI
     * @return Optional Index if found, empty Optional if nothing is found.
     */
    public Optional<WaveformIndex> get(String fileURI) {
        try {
            GetResponse result = client.get(new GetRequest(ES_WF_INDEX, ES_WF_TYPE, fileURI),
                    RequestOptions.DEFAULT);
            if (result.isExists())
            {
                return Optional.of(mapper.readValue(result.getSourceAsBytesRef().streamInput(), WaveformIndex.class));
            } else
            {
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Checkif the the waveformIndex identified by fileURI exists
     * @param fileURI
     * @return true if the Index for fileURI exists
     */
    public boolean checkExists(String fileURI) {
        try {
            return client.exists(new GetRequest(ES_WF_INDEX, ES_WF_TYPE, fileURI), RequestOptions.DEFAULT);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to find index for file: " + fileURI, e);
        }
    }

    /**
     * Search for waveformIndex's
     * @param searchParameters
     * @return list of {@link WaveformIndex}s which match the search parameters
     */
    public List<WaveformIndex> search(MultiValueMap<String, String> searchParameters) {

        SearchRequest searchRequest = new SearchRequest(ES_WF_INDEX+"*");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        try
        {
            List<WaveformIndex> result = new ArrayList<WaveformIndex>();

            for (Entry<String, List<String>> parameter : searchParameters.entrySet())
            {
                switch (parameter.getKey().strip().toLowerCase()) {
                    case "file":
                        DisMaxQueryBuilder fileQuery = disMaxQuery();
                        for (String value : parameter.getValue()) {
                            for (String pattern : value.split("[\\|,;]")) {
                                fileQuery.add(wildcardQuery("file", pattern.trim()));
                            }
                        }
                        boolQuery.must(fileQuery);
                        break;
                    case "tags":
                        DisMaxQueryBuilder tagQuery = disMaxQuery();
                        for (String value : parameter.getValue()) {
                            for (String pattern : value.split("[\\|,;]")) {
                                tagQuery.add(wildcardQuery("tags.name", pattern.trim()));
                            }
                        }
                        boolQuery.must(nestedQuery("tags", tagQuery, ScoreMode.None));
                        break;
                }

            }

            searchSourceBuilder.query(boolQuery);
            searchSourceBuilder.from(0);
            searchSourceBuilder.size(100);

            searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

            searchRequest.source(searchSourceBuilder);

            final SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            searchResponse.getHits().forEach(hit -> {
                try
                {
                    result.add(mapper.readValue(hit.getSourceAsString(), WaveformIndex.class));
                } catch (IOException e)
                {
                    logger.log(Level.SEVERE, "Failed to parse result for search : " + searchParameters, e);
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to parse result for search : " + searchParameters + ", CAUSE: " + e.getMessage(),
                            e);
                }
            });
            return result;
        } catch (IOException e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to complete search");
        }
    }

    /**
     * Create a WaveformIndex
     * @param entity the {@link WaveformIndex} to be created
     * @return the created Index
     */
    public WaveformIndex save(WaveformIndex entity) {
        try {
            // Process the entity using the registered processors
            Iterator<ProcessWaveformIndex> iterator = loader.iterator();
            while (iterator.hasNext()) {
                ProcessWaveformIndex process = iterator.next();
                entity = process.process(entity);
            }

            IndexRequest indexRequest = new IndexRequest(ES_WF_INDEX, ES_WF_TYPE, entity.getFile().toString())
                    .source(mapper.writeValueAsBytes(entity), XContentType.JSON);
            indexRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            IndexResponse response = client.index(indexRequest, RequestOptions.DEFAULT);
            if (response.getResult().equals(Result.CREATED) ||
                response.getResult().equals(Result.UPDATED)) {
                BytesReference ref = client.get(new GetRequest(ES_WF_INDEX, ES_WF_TYPE, response.getId()),
                        RequestOptions.DEFAULT).getSourceAsBytesRef();
                return mapper.readValue(ref.streamInput(), WaveformIndex.class);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    public WaveformIndex addTag(WaveformIndex entity, WaveformFileTag tag) {
        try {
            entity.addTag(tag);
            UpdateRequest updateRequest = new UpdateRequest(ES_WF_INDEX, ES_WF_TYPE, entity.getFile().toString());
            IndexRequest indexRequest = new IndexRequest(ES_WF_INDEX, ES_WF_TYPE, entity.getFile().toString())
                                                .source(mapper.writeValueAsBytes(entity), XContentType.JSON);
            updateRequest.doc(mapper.writeValueAsBytes(entity), XContentType.JSON).upsert(indexRequest);
            updateRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            UpdateResponse response = client.update(updateRequest, RequestOptions.DEFAULT);
            if (response.getResult().equals(Result.CREATED) ||
                response.getResult().equals(Result.UPDATED)) {
                BytesReference ref = client.get(new GetRequest(ES_WF_INDEX, ES_WF_TYPE, response.getId()),
                        RequestOptions.DEFAULT).getSourceAsBytesRef();
                WaveformIndex responseIndex = mapper.readValue(ref.streamInput(), WaveformIndex.class);
                return responseIndex;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    public WaveformIndex addProperty(WaveformIndex entity, WaveformFileProperty property) {
        try {
            entity.addProperty(property);
            UpdateRequest updateRequest = new UpdateRequest(ES_WF_INDEX, ES_WF_TYPE, entity.getFile().toString());
            IndexRequest indexRequest = new IndexRequest(ES_WF_INDEX, ES_WF_TYPE, entity.getFile().toString())
                    .source(mapper.writeValueAsBytes(entity), XContentType.JSON);
            updateRequest.doc(mapper.writeValueAsBytes(entity), XContentType.JSON).upsert(indexRequest);
            updateRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            UpdateResponse response = client.update(updateRequest, RequestOptions.DEFAULT);
            if (response.getResult().equals(Result.CREATED) ||
                    response.getResult().equals(Result.UPDATED)) {
                BytesReference ref = client.get(new GetRequest(ES_WF_INDEX, ES_WF_TYPE, response.getId()),
                        RequestOptions.DEFAULT).getSourceAsBytesRef();
                WaveformIndex responseIndex = mapper.readValue(ref.streamInput(), WaveformIndex.class);
                return responseIndex;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    public WaveformIndex addPvProperty(WaveformIndex entity, WaveformFilePVProperty pvProperty) {
        try {
            entity.addPvProperty(pvProperty);
            UpdateRequest updateRequest = new UpdateRequest(ES_WF_INDEX, ES_WF_TYPE, entity.getFile().toString());
            IndexRequest indexRequest = new IndexRequest(ES_WF_INDEX, ES_WF_TYPE, entity.getFile().toString())
                    .source(mapper.writeValueAsBytes(entity), XContentType.JSON);
            updateRequest.doc(mapper.writeValueAsBytes(entity), XContentType.JSON).upsert(indexRequest);
            updateRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            UpdateResponse response = client.update(updateRequest, RequestOptions.DEFAULT);
            if (response.getResult().equals(Result.CREATED) ||
                    response.getResult().equals(Result.UPDATED)) {
                BytesReference ref = client.get(new GetRequest(ES_WF_INDEX, ES_WF_TYPE, response.getId()),
                        RequestOptions.DEFAULT).getSourceAsBytesRef();
                WaveformIndex responseIndex = mapper.readValue(ref.streamInput(), WaveformIndex.class);
                return responseIndex;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Delete the waveformIndex, if it exists
     * @param waveformIndex the index to be deleted
     */
    public void delete(WaveformIndex waveformIndex) {

        try {
            DeleteResponse response = client.delete(
                    new DeleteRequest(ES_WF_INDEX, ES_WF_TYPE, waveformIndex.getFile().toString()), RequestOptions.DEFAULT);
        } catch (DocumentMissingException e) {
            logger.log(Level.SEVERE, waveformIndex.getFile() + " Does not exist and thus cannot be deleted");
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * Delete the waveformIndex identified by the id fileURI
     * @param fileURI the fileURI id to be deleted
     */
    public void delete(String fileURI) {
        try {
            DeleteResponse response = client.delete(
                    new DeleteRequest(ES_WF_INDEX, ES_WF_TYPE, fileURI), RequestOptions.DEFAULT);
        } catch (DocumentMissingException e) {
            logger.log(Level.SEVERE, fileURI + " Does not exist and thus cannot be deleted");
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public WaveformIndex find(Map<String, String> searchParameters) {
        return null;
    }

    public List<WaveformIndex> findAll() {
        return null;
    }

}
