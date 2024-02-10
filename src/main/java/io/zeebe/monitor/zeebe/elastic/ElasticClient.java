package io.zeebe.monitor.zeebe.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.rest_client.RestClientTransport;

public record ElasticClient(
        RestClientTransport transport,
        ElasticsearchClient client
) {
}
