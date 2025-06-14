package org.pc28.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.pc28.constants.WebConstants;
import org.pc28.exception.WebDownloadException;
import org.pc28.model.WebDownloadResult;
import org.pc28.service.WebDownloadService;
import org.pc28.utils.FileUtils;
import org.pc28.utils.HttpUtils;
import org.springframework.stereotype.Service;

/**
 * 网页下载服务实现类
 */
@Service
@Slf4j
public class WebDownloadServiceImpl implements WebDownloadService {
    
    /**
     * 下载网页并保存到本地
     *
     * @param url 网页URL
     * @param saveDirectory 保存目录
     * @param fileName 文件名
     * @return 下载结果
     */
    @Override
    public WebDownloadResult downloadAndSave(String url, String saveDirectory, String fileName) {
        log.info("============================================");
        log.info("【步骤1】开始下载网页任务");
        log.info("网页URL: {}", url);
        log.info("保存目录: {}", saveDirectory);
        log.info("保存文件名: {}", fileName);
        log.info("============================================");
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 发送HTTP请求下载网页
            log.info("【步骤2】正在建立HTTP连接...");
            String content = HttpUtils.sendGetRequest(url);
            
            if (content == null || content.isEmpty()) {
                log.warn("【错误】获取到的网页内容为空");
                return WebDownloadResult.failure("获取到的网页内容为空");
            }
            
            log.info("【步骤3】已成功获取网页内容，大小: {} 字节", content.length());
            
            // 保存内容到文件
            log.info("【步骤4】准备将网页内容保存到文件...");
            FileUtils.saveToFile(saveDirectory, fileName, content);
            
            // 获取文件绝对路径
            String filePath = FileUtils.getAbsolutePath(saveDirectory, fileName);
            log.info("【步骤5】文件已成功保存，完整路径: {}", filePath);
            
            long endTime = System.currentTimeMillis();
            long downloadTimeMillis = endTime - startTime;
            
            log.info("============================================");
            log.info("【步骤6】网页下载任务完成");
            log.info("耗时: {}毫秒", downloadTimeMillis);
            log.info("内容长度: {}字节", content.length());
            log.info("保存位置: {}", filePath);
            log.info("============================================");
            
            return WebDownloadResult.success(content, filePath, downloadTimeMillis);
        } catch (WebDownloadException e) {
            log.error("【错误】网页下载过程中发生异常: {}", e.getMessage(), e);
            return WebDownloadResult.failure(e.getMessage());
        } catch (Exception e) {
            log.error("【错误】网页下载过程中发生未预期的异常: {}", e.getMessage(), e);
            return WebDownloadResult.failure("下载过程中发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 使用默认参数下载网页并保存到本地
     *
     * @return 下载结果
     */
    @Override
    public WebDownloadResult downloadAndSaveDefault() {
        log.info("使用默认参数执行网页下载");
        // 使用第一个URL和带时间戳的文件名
        String fileName = WebConstants.getTimestampedFileName(0);
        return downloadAndSave(
                WebConstants.TARGET_URL,
                WebConstants.SAVE_PATH,
                fileName
        );
    }
    
    /**
     * 下载指定索引的URL并使用带时间戳的文件名保存
     *
     * @param urlIndex URL索引
     * @return 下载结果
     */
    public WebDownloadResult downloadWithTimestamp(int urlIndex) {
        if (urlIndex < 0 || urlIndex >= WebConstants.TARGET_URLS.length) {
            return WebDownloadResult.failure("无效的URL索引: " + urlIndex);
        }
        
        String url = WebConstants.TARGET_URLS[urlIndex];
        String fileName = WebConstants.getTimestampedFileName(urlIndex);
        
        log.info("执行带时间戳的网页下载，URL索引: {}", urlIndex);
        log.info("目标URL: {}", url);
        log.info("文件名: {}", fileName);
        
        return downloadAndSave(url, WebConstants.SAVE_PATH, fileName);
    }
    
    /**
     * 下载所有配置的URL
     *
     * @return 成功下载的URL数量
     */
    public int downloadAllUrls() {
        int successCount = 0;
        
        for (int i = 0; i < WebConstants.TARGET_URLS.length; i++) {
            WebDownloadResult result = downloadWithTimestamp(i);
            if (result.isSuccess()) {
                successCount++;
                log.info("成功下载 URL[{}]: {}", i, WebConstants.TARGET_URLS[i]);
            } else {
                log.error("下载 URL[{}] 失败: {}", i, result.getErrorMessage());
            }
        }
        
        return successCount;
    }
}