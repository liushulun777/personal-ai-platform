package com.platform.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * ElasticSearch 配置
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.platform.search.repository")
public class ElasticsearchConfig {

    @Value("${spring.data.elasticsearch.uris}")
    private String elasticsearchUris;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        String[] parts = elasticsearchUris.replace("http://", "").replace("https://", "").split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        RestClient restClient = RestClient.builder(
                new HttpHost(host, port, "http")
        ).build();

        // 配置 ObjectMapper：忽略未知字段（如 _class）、支持 LocalDateTime
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        RestClientTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper(objectMapper)
        );

        return new ElasticsearchClient(transport);
    }
}
