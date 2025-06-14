package org.pc28.shell;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pc28.constants.WebConstants;
import org.pc28.model.WebDownloadResult;
import org.pc28.service.WebDownloadService;
import org.pc28.service.impl.WebDownloadServiceImpl;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * 网页下载相关命令
 */
@ShellComponent
@Slf4j
@RequiredArgsConstructor
public class WebDownloadCommands {

    private final WebDownloadServiceImpl webDownloadService;
    
    /**
     * 下载单个网页命令
     */
    @ShellMethod(key = {"1", "download"}, value = "下载目标网页(默认第一个)")  
    public String downloadWebpage() {
        log.info("======== 开始执行网页下载任务 ========");
        log.info("步骤1: 准备下载参数");
        log.info("目标网址: {}", WebConstants.TARGET_URL);
        log.info("保存目录: {}", WebConstants.SAVE_PATH);
        
        log.info("步骤2: 初始化HTTP客户端连接");
        log.info("步骤3: 发送HTTP请求并获取网页内容");
        
        WebDownloadResult result = webDownloadService.downloadAndSaveDefault();
        
        if (result.isSuccess()) {
            log.info("步骤4: 网页内容获取成功，大小: {} 字节", result.getContent().length());
            log.info("步骤5: 创建目标目录: {}", WebConstants.SAVE_PATH);
            log.info("步骤6: 将内容写入文件: {}", result.getFilePath());
            log.info("======== 网页下载任务完成 ========");
            return String.format(
                "下载成功！\n" +
                "目标网址: %s\n" +
                "文件保存路径: %s\n" +
                "下载耗时: %d毫秒\n" +
                "内容大小: %d字节",
                WebConstants.TARGET_URL,
                result.getFilePath(),
                result.getDownloadTimeMillis(),
                result.getContent().length()
            );
        } else {
            log.error("步骤4: 下载过程中发生错误");
            log.error("错误信息: {}", result.getErrorMessage());
            log.error("======== 网页下载任务失败 ========");
            return String.format("下载失败: %s", result.getErrorMessage());
        }
    }
    
    /**
     * 下载指定索引的网页
     */
    @ShellMethod(key = {"2", "download-index"}, value = "指定索引下载目标网页(0-5)")
    public String downloadByIndex(@ShellOption(value = {"-i", "--index"}, help = "网页索引值") int index) {
        if (index < 0 || index >= WebConstants.TARGET_URLS.length) {
            return String.format("错误: 无效的索引值 %d，有效范围是 0-%d", 
                    index, WebConstants.TARGET_URLS.length - 1);
        }
        
        log.info("======== 开始执行索引 {} 网页下载任务 ========", index);
        log.info("目标网址: {}", WebConstants.TARGET_URLS[index]);
        
        WebDownloadResult result = webDownloadService.downloadWithTimestamp(index);
        
        if (result.isSuccess()) {
            return String.format(
                "下载成功！\n" +
                "索引: %d\n" +
                "目标网址: %s\n" +
                "文件保存路径: %s\n" +
                "下载耗时: %d毫秒\n" +
                "内容大小: %d字节",
                index,
                WebConstants.TARGET_URLS[index],
                result.getFilePath(),
                result.getDownloadTimeMillis(),
                result.getContent().length()
            );
        } else {
            return String.format("下载失败: %s", result.getErrorMessage());
        }
    }
    
    /**
     * 下载所有目标网页
     */
    @ShellMethod(key = {"3", "download-all"}, value = "下载所有目标网页")
    public String downloadAllWebpages() {
        log.info("======== 开始执行所有网页下载任务 ========");
        log.info("总目标数量: {}", WebConstants.TARGET_URLS.length);
        
        int successCount = webDownloadService.downloadAllUrls();
        
        log.info("======== 网页下载任务完成 ========");
        
        return String.format(
            "下载结果:\n" +
            "总目标数量: %d\n" +
            "成功下载: %d\n" +
            "失败下载: %d\n" +
            "保存目录: %s",
            WebConstants.TARGET_URLS.length,
            successCount,
            WebConstants.TARGET_URLS.length - successCount,
            WebConstants.SAVE_PATH
        );
    }
}