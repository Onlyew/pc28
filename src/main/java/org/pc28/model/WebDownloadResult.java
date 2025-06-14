package org.pc28.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 网页下载结果模型类
 */
@Data
@Builder
@Schema(description = "网页下载结果")
public class WebDownloadResult {
    
    /**
     * 是否成功
     */
    @Schema(description = "下载是否成功", example = "true")
    private boolean success;
    
    /**
     * 下载的网页内容
     */
    @Schema(description = "下载的网页内容", example = "<html><body>网页内容</body></html>")
    private String content;
    
    /**
     * 保存的文件路径
     */
    @Schema(description = "保存的文件路径", example = "E:/pc28/download/redtaotao.html")
    private String filePath;
    
    /**
     * 错误信息（如果有）
     */
    @Schema(description = "错误信息（如果下载失败）", example = "连接超时")
    private String errorMessage;
    
    /**
     * 下载时间（毫秒）
     */
    @Schema(description = "下载耗时（毫秒）", example = "1245")
    private long downloadTimeMillis;
    
    /**
     * 创建成功结果
     * 
     * @param content 下载的内容
     * @param filePath 文件路径
     * @param downloadTimeMillis 下载时间
     * @return 下载结果对象
     */
    public static WebDownloadResult success(String content, String filePath, long downloadTimeMillis) {
        return WebDownloadResult.builder()
                .success(true)
                .content(content)
                .filePath(filePath)
                .downloadTimeMillis(downloadTimeMillis)
                .build();
    }
    
    /**
     * 创建失败结果
     * 
     * @param errorMessage 错误信息
     * @return 下载结果对象
     */
    public static WebDownloadResult failure(String errorMessage) {
        return WebDownloadResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}