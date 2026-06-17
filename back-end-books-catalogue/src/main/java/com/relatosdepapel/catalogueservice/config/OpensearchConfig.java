package com.relatosdepapel.catalogueservice.config;

import org.opensearch.client.RestHighLevelClient;
import org.opensearch.data.client.orhlc.AbstractOpenSearchConfiguration;
import org.opensearch.data.client.orhlc.ClientConfiguration;
import org.opensearch.data.client.orhlc.RestClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(
        basePackages = "com.relatosdepapel.catalogueservice.repository.elastic"
)
public class OpensearchConfig extends AbstractOpenSearchConfiguration {

    @Value("${opensearch.host}")
    private String clusterEndpoint;

    @Value("${opensearch.credentials.user}")
    private String username;

    @Value("${opensearch.credentials.password}")
    private String password;

    @Override
    public RestHighLevelClient opensearchClient() {
        String host = normalizeHost(clusterEndpoint);

        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(host)
                .usingSsl()
                .withBasicAuth(username, password)
                .build();

        return RestClients.create(clientConfiguration).rest();
    }

    private String normalizeHost(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("La variable opensearch.host es obligatoria");
        }

        String host = value.trim()
                .replace("https://", "")
                .replace("http://", "");

        if (!host.contains(":")) {
            host = host + ":443";
        }

        return host;
    }
}