package org.phoebus.services.waveform.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesRequest;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesResponse;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import static org.phoebus.services.waveform.index.WaveformIndexService.*;
/**
 * A element which creates the elastic rest clients used the olog service for
 * creating and retrieving logs and other resources
 *
 * @author kunal
 */
@Configuration
@ComponentScan(basePackages = {"org.phoebus"})
@PropertySource("classpath:application.properties")
public class ElasticConfig {
    private RestHighLevelClient indexClient;

    // Read the elatic index and type from the application.properties
    @Value("${elasticsearch.wf.index:waveform_index}")
    private String ES_WF_INDEX;
    @Value("${elasticsearch.wf.type:waveform_index}")
    private String ES_WF_TYPE;

    @Value("${elasticsearch.cluster.name:elasticsearch}")
    private String clusterName;
    @Value("${elasticsearch.network.host:localhost}")
    private String host;
    @Value("${elasticsearch.http.port:9200}")
    private int port;

    /**
     *
     * @return an REST client for elastic search
     */
    @Bean({ "indexClient" })
    public RestHighLevelClient getIndexClient()
    {
        if (indexClient == null)
        {
            indexClient = new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, "http")));
            elasticIndexValidation(indexClient);
        }
        return indexClient;
    }


    /**
     * Checks for the existence of the elastic index needed for waveform index service and creates
     * it with the appropriate mapping if it is missing.
     *
     * @param indexClient the elastic client instance used to validate and create
     *                    olog indices
     */
    private synchronized void elasticIndexValidation(RestHighLevelClient indexClient) {
        GetIndexTemplatesResponse templates = null;
        try {
            templates = indexClient.indices().getTemplate(new GetIndexTemplatesRequest("*"), RequestOptions.DEFAULT);
            if (!templates.getIndexTemplates().stream().anyMatch(i -> {
                return i.get().getName().equalsIgnoreCase(ES_WF_INDEX + "_template");
            })) {
                logger.info("Waveform Index template is missing, adding it now..");

                {
                    PutIndexTemplateRequest templateRequest = new PutIndexTemplateRequest(ES_WF_INDEX + "_template");

                    templateRequest.patterns(Arrays.asList(ES_WF_INDEX));

                    ObjectMapper mapper = new ObjectMapper();
                    InputStream is = ElasticConfig.class.getResourceAsStream("/wf_index_template_mapping.json");

                    Map<String, String> jsonMap = mapper.readValue(is, Map.class);
                    templateRequest.mapping(ES_WF_TYPE, XContentFactory.jsonBuilder().map(jsonMap));
                    templateRequest.create(true);
                    indexClient.indices().putTemplate(templateRequest, RequestOptions.DEFAULT);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}