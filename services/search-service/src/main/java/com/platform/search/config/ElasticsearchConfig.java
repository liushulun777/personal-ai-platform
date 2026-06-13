package com.platform.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
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

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUris;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        // 解析 URI
        String[] parts = elasticsearchUris.replace("http://", "").replace("https://", "").split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        // 创建 RestClient
        RestClient restClient = RestClient.builder(
                new HttpHost(host, port, "http")
        ).build();

        // 创建传输层
        RestClientTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper()
        );

        // 创建客户端
        return new ElasticsearchClient(transport);
    }
}
