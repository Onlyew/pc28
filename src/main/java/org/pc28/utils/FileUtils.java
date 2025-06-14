package org.pc28.utils;

import lombok.extern.slf4j.Slf4j;
import org.pc28.exception.WebDownloadException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件操作工具类
 */
@Slf4j
public class FileUtils {
    
    private FileUtils() {
        // 私有构造函数，防止实例化
    }
    
    /**
     * 将内容保存到文件
     *
     * @param directory 目录路径
     * @param fileName 文件名
     * @param content 文件内容
     * @throws WebDownloadException 如果保存失败
     */
    public static void saveToFile(String directory, String fileName, String content) throws WebDownloadException {
        log.info("【文件步骤1】准备保存文件，目录: {}, 文件名: {}, 内容大小: {}字节", directory, fileName, content.length());
        
        try {
            // 创建目录
            Path dirPath = Paths.get(directory);
            if (!Files.exists(dirPath)) {
                log.info("【文件步骤2】目录不存在，正在创建目录: {}", dirPath);
                Files.createDirectories(dirPath);
                log.info("【文件步骤2】目录创建成功: {}", dirPath);
            } else {
                log.info("【文件步骤2】目录已存在: {}", dirPath);
            }
            
            // 创建文件路径
            Path filePath = dirPath.resolve(fileName);
            log.info("【文件步骤3】准备写入文件: {}", filePath.toAbsolutePath());
            
            // 写入文件
            Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
            log.info("【文件步骤4】文件写入成功: {}, 大小: {}字节", filePath.toAbsolutePath(), content.length());
        } catch (IOException e) {
            log.error("【文件错误】保存文件时发生IO异常: {}", e.getMessage(), e);
            throw new WebDownloadException("保存文件失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("【文件错误】保存文件时发生未预期的异常: {}", e.getMessage(), e);
            throw new WebDownloadException("保存文件发生未预期的异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取文件的绝对路径
     *
     * @param directory 目录路径
     * @param fileName 文件名
     * @return 文件的绝对路径
     */
    public static String getAbsolutePath(String directory, String fileName) {
        Path filePath = Paths.get(directory, fileName);
        String absolutePath = filePath.toAbsolutePath().toString();
        log.info("【文件路径】文件的绝对路径: {}", absolutePath);
        return absolutePath;
    }
}