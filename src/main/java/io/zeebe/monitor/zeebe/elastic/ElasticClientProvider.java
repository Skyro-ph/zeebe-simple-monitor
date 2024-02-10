package io.zeebe.monitor.zeebe.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "zeebe-importer", havingValue = "elastic")
public class ElasticClientProvider {
    @Value("${elastic.url}")
    private String elasticUrl;

    @Value("${elastic.user}")
    private String elasticUser;

    @Value("${elastic.password}")
    private String elasticPassword;

    public ElasticClient getClient() {
        final CredentialsProvider credentialsProvider =
                new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(elasticUser, elasticPassword));

        RestClient restClient = RestClient
                .builder(HttpHost.create(elasticUrl))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider))
                .build();
        var transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        var client = new ElasticsearchClient(transport);

        return new ElasticClient(transport, client);
    }
}
