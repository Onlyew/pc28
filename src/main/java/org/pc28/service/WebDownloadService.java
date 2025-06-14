package org.pc28.service;

import org.pc28.model.WebDownloadResult;

/**
 * 网页下载服务接口
 */
public interface WebDownloadService {
    
    /**
     * 下载网页并保存到本地
     * 
     * @param url 网页URL
     * @param saveDirectory 保存目录
     * @param fileName 文件名
     * @return 下载结果
     */
    WebDownloadResult downloadAndSave(String url, String saveDirectory, String fileName);
    
    /**
     * 使用默认参数下载网页并保存到本地
     * 
     * @return 下载结果
     */
    WebDownloadResult downloadAndSaveDefault();
    
    /**
     * 下载指定索引的URL并使用带时间戳的文件名保存
     *
     * @param urlIndex URL索引
     * @return 下载结果
     */
    WebDownloadResult downloadWithTimestamp(int urlIndex);
    
    /**
     * 下载所有配置的URL
     *
     * @return 成功下载的URL数量
     */
    int downloadAllUrls();
}