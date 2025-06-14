package org.pc28.config;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.pc28.constants.WebConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HTTP客户端配置类
 */
@Configuration
public class HttpClientConfig {
    
    /**
     * 最大连接数
     */
    private static final int MAX_TOTAL_CONNECTIONS = 100;
    
    /**
     * 每个路由的最大连接数
     */
    private static final int MAX_CONNECTIONS_PER_ROUTE = 10;
    
    /**
     * 创建连接池管理器
     * 
     * @return 连接池管理器
     */
    @Bean
    public PoolingHttpClientConnectionManager connectionManager() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        connectionManager.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE);
        return connectionManager;
    }
    
    /**
     * 创建请求配置
     * 
     * @return 请求配置
     */
    @Bean
    public RequestConfig requestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(WebConstants.CONNECTION_TIMEOUT)
                .setSocketTimeout(WebConstants.READ_TIMEOUT)
                .setConnectionRequestTimeout(WebConstants.CONNECTION_TIMEOUT)
                .build();
    }
    
    /**
     * 创建HTTP客户端
     * 
     * @param connectionManager 连接池管理器
     * @param requestConfig 请求配置
     * @return HTTP客户端
     */
    @Bean
    public CloseableHttpClient httpClient(PoolingHttpClientConnectionManager connectionManager, RequestConfig requestConfig) {
        return HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }
}