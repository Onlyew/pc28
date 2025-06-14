package org.pc28.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.pc28.constants.WebConstants;
import org.pc28.exception.WebDownloadException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * HTTP请求工具类
 */
@Slf4j
public class HttpUtils {
    
    private HttpUtils() {
        // 私有构造函数，防止实例化
    }
    
    /**
     * 发送HTTP GET请求
     *
     * @param url 请求URL
     * @return 响应内容
     * @throws WebDownloadException 如果请求失败
     */
    public static String sendGetRequest(String url) throws WebDownloadException {
        return sendGetRequestWithRetry(url, WebConstants.MAX_RETRY_COUNT);
    }
    
    /**
     * 发送带重试机制的HTTP GET请求
     *
     * @param url 请求URL
     * @param maxRetries 最大重试次数
     * @return 响应内容
     * @throws WebDownloadException 如果所有重试都失败
     */
    public static String sendGetRequestWithRetry(String url, int maxRetries) throws WebDownloadException {
        log.info("【HTTP步骤1】准备发送GET请求到URL: {}, 最大重试次数: {}", url, maxRetries);
        log.info("【HTTP步骤1】请求超时设置: 连接超时={}ms, 读取超时={}ms", WebConstants.CONNECTION_TIMEOUT, WebConstants.READ_TIMEOUT);
        log.info("【HTTP步骤1】User-Agent: {}", WebConstants.USER_AGENT);
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            log.info("【HTTP步骤2】已创建HTTP客户端");
            
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("User-Agent", WebConstants.USER_AGENT);
            
            // 设置请求配置
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(WebConstants.CONNECTION_TIMEOUT)
                    .setSocketTimeout(WebConstants.READ_TIMEOUT)
                    .build();
            httpGet.setConfig(requestConfig);
            log.info("【HTTP步骤3】HTTP请求已配置好，准备发送");
            
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                log.info("【HTTP步骤4】收到HTTP响应，状态码: {}", statusCode);
                
                if (statusCode != 200) {
                    log.error("【HTTP错误】HTTP请求失败，状态码: {}", statusCode);
                    throw new WebDownloadException("HTTP请求失败，状态码: " + statusCode);
                }
                
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    log.error("【HTTP错误】HTTP响应内容为空");
                    throw new WebDownloadException("HTTP响应内容为空");
                }
                
                log.info("【HTTP步骤5】开始读取响应内容...");
                String content = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                log.info("【HTTP步骤6】成功读取响应内容，大小: {} 字节", content.length());
                
                return content;
            }
        } catch (IOException e) {
            log.error("【HTTP错误】HTTP请求发生IO异常: {}", e.getMessage(), e);
            
            // 如果还有重试次数，则重试
            if (maxRetries > 0) {
                log.info("【HTTP重试】将在{}ms后重试, 剩余重试次数: {}", WebConstants.RETRY_WAIT_TIME, maxRetries - 1);
                
                try {
                    Thread.sleep(WebConstants.RETRY_WAIT_TIME);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("【HTTP错误】重试等待被中断", ie);
                }
                
                return sendGetRequestWithRetry(url, maxRetries - 1);
            }
            
            throw new WebDownloadException("HTTP请求失败，已重试最大次数: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("【HTTP错误】HTTP请求发生未预期的异常: {}", e.getMessage(), e);
            
            // 对于非IO异常，也提供重试机制
            if (maxRetries > 0) {
                log.info("【HTTP重试】将在{}ms后重试, 剩余重试次数: {}", WebConstants.RETRY_WAIT_TIME, maxRetries - 1);
                
                try {
                    Thread.sleep(WebConstants.RETRY_WAIT_TIME);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("【HTTP错误】重试等待被中断", ie);
                }
                
                return sendGetRequestWithRetry(url, maxRetries - 1);
            }
            
            throw new WebDownloadException("HTTP请求发生未预期的异常，已重试最大次数: " + e.getMessage(), e);
        }
    }
}