package org.pc28.shell;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pc28.constants.WebConstants;
import org.pc28.model.DrawResult;
import org.pc28.service.db.HistoryDataService;
import org.pc28.utils.HtmlParser;
import org.pc28.utils.SimpleHtmlParser;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * HTML解析相关命令
 */
@ShellComponent
@Slf4j
@RequiredArgsConstructor
public class HtmlParserCommands {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HtmlParser htmlParser = new HtmlParser();
    private final HistoryDataService historyDataService;
    
    /**
     * 解析HTML文件中的开奖数据
     */
    @ShellMethod(key = {"4", "parse"}, value = "解析HTML文件中的开奖数据")
    public String parseHtml(
            @ShellOption(value = {"-f", "--file"}, defaultValue = "download/redtaotao.html") String htmlFilePath,
            @ShellOption(value = {"-p", "--pretty"}, defaultValue = "true") boolean prettyPrint,
            @ShellOption(value = {"-s", "--structured"}, defaultValue = "false") boolean structured) {
        
        log.info("======== 开始执行HTML解析任务 ========");
        log.info("HTML文件路径: {}", htmlFilePath);
        log.info("是否使用结构化对象: {}", structured);
        
        try {
            File file = new File(htmlFilePath);
            if (!file.exists()) {
                log.error("HTML文件不存在: {}", htmlFilePath);
                return "错误: 文件不存在 - " + htmlFilePath;
            }
            
            log.info("正在解析HTML文件...");
            
            String json;
            if (structured) {
                log.info("使用结构化对象模式解析");
                DrawResult result = htmlParser.parseDrawResultObject(htmlFilePath);
                if (prettyPrint) {
                    json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
                } else {
                    json = objectMapper.writeValueAsString(result);
                }
            } else {
                log.info("使用Map模式解析");
                Map<String, Object> result = htmlParser.parseDrawData(htmlFilePath);
                if (prettyPrint) {
                    json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
                } else {
                    json = objectMapper.writeValueAsString(result);
                }
            }
            
            log.info("======== HTML解析任务完成 ========");
            return json;
            
        } catch (IOException e) {
            log.error("解析HTML文件时发生错误: {}", e.getMessage(), e);
            return "解析错误: " + e.getMessage();
        }
    }
    
    /**
     * 使用CSS选择器解析HTML文件
     */
    @ShellMethod(key = {"5", "selector"}, value = "使用CSS选择器解析HTML文件")
    public String parseWithSelector(
            @ShellOption(value = {"-f", "--file"}, defaultValue = "download/redtaotao.html") String htmlFilePath,
            @ShellOption(value = {"-s", "--selector"}, defaultValue = ".kj-item") String cssSelector) {
        
        log.info("======== 开始执行HTML选择器解析任务 ========");
        log.info("HTML文件路径: {}", htmlFilePath);
        log.info("CSS选择器: {}", cssSelector);
        
        try {
            File file = new File(htmlFilePath);
            if (!file.exists()) {
                log.error("HTML文件不存在: {}", htmlFilePath);
                return "错误: 文件不存在 - " + htmlFilePath;
            }
            
            log.info("正在使用选择器解析HTML文件...");
            String result = htmlParser.parseBySelector(htmlFilePath, cssSelector);
            
            log.info("======== HTML选择器解析任务完成 ========");
            return result;
            
        } catch (IOException e) {
            log.error("解析HTML文件时发生错误: {}", e.getMessage(), e);
            return "解析错误: " + e.getMessage();
        }
    }
    
    /**
     * 解析XPath格式的路径（将XPath转换为CSS选择器）
     */
    @ShellMethod(key = {"6", "xpath"}, value = "使用XPath解析HTML文件")
    public String parseWithXPath(
            @ShellOption(value = {"-f", "--file"}, defaultValue = "download/redtaotao.html") String htmlFilePath,
            @ShellOption(value = {"-x", "--xpath"}) String xpath) {
        
        log.info("======== 开始执行HTML XPath解析任务 ========");
        log.info("HTML文件路径: {}", htmlFilePath);
        log.info("XPath: {}", xpath);
        
        try {
            // 简单的XPath转CSS选择器
            String cssSelector = convertXPathToCssSelector(xpath);
            log.info("转换后的CSS选择器: {}", cssSelector);
            
            return parseWithSelector(htmlFilePath, cssSelector);
            
        } catch (Exception e) {
            log.error("解析XPath时发生错误: {}", e.getMessage(), e);
            return "XPath解析错误: " + e.getMessage();
        }
    }
    
    /**
     * 简单的XPath转化CSS选择器
     * 注意：这只是一个简单的转换，只能处理基本XPath表达式
     */
    private String convertXPathToCssSelector(String xpath) {
        // 处理 /html/body/div[1]/div/div[3] 这样的简单XPath
        if (xpath.startsWith("/")) {
            xpath = xpath.substring(1); // 移除开头的 /
        }
        
        String[] parts = xpath.split("/");
        StringBuilder cssSelector = new StringBuilder();
        
        for (String part : parts) {
            if (part.isEmpty()) continue;
            
            // 处理 element[index] 形式
            if (part.contains("[") && part.contains("]")) {
                String tagName = part.substring(0, part.indexOf("["));
                String index = part.substring(part.indexOf("[") + 1, part.indexOf("]"));
                
                // 在CSS中，:nth-of-type 是从1开始的
                cssSelector.append(tagName).append(":nth-of-type(").append(index).append(")").append(" ");
            } else {
                cssSelector.append(part).append(" ");
            }
        }
        
        return cssSelector.toString().trim();
    }
    
    /**
     * 清空下载文件夹中的所有HTML文件
     * 
     * @return 删除的文件数量
     */
    private int cleanDownloadFolder() {
        log.info("开始清空下载文件夹...");
        File downloadDir = new File(WebConstants.SAVE_PATH);
        if (!downloadDir.exists() || !downloadDir.isDirectory()) {
            log.warn("下载文件夹不存在: {}", WebConstants.SAVE_PATH);
            return 0;
        }
        
        File[] htmlFiles = downloadDir.listFiles((dir, name) -> name.endsWith(WebConstants.FILE_EXTENSION));
        if (htmlFiles == null || htmlFiles.length == 0) {
            log.info("下载文件夹中没有HTML文件需要删除");
            return 0;
        }
        
        int deletedCount = 0;
        for (File htmlFile : htmlFiles) {
            if (htmlFile.delete()) {
                log.debug("已删除文件: {}", htmlFile.getName());
                deletedCount++;
            } else {
                log.warn("无法删除文件: {}", htmlFile.getName());
            }
        }
        
        log.info("下载文件夹清理完成，共删除 {} 个HTML文件", deletedCount);
        return deletedCount;
    }
    
    /**
     * 解析所有服务器的最新HTML文件
     */
    @ShellMethod(key = {"7", "parse-all"}, value = "解析所有服务器的最新数据")
    public String parseAllServers(
            @ShellOption(value = {"-s", "--save"}, defaultValue = "true", 
                help = "是否保存到数据库") boolean saveToDatabase) {
        log.info("======== 开始解析所有服务器的数据 ========");
        
        try {
            List<Map<String, Object>> allResults = SimpleHtmlParser.parseAllServers();
            SimpleHtmlParser.compareServerResults(allResults);
            
            // 转换为JSON格式以便深入分析
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(allResults);
            
            // 保存到数据库
            StringBuilder resultMessage = new StringBuilder();
            resultMessage.append(String.format("解析结果:\n服务器数: %d\n", allResults.size()));
            
            if (saveToDatabase) {
                log.info("开始将解析结果保存到数据库...");
                Map<String, Integer> savedCounts = historyDataService.saveAllServersData(allResults);
                
                resultMessage.append("数据库保存结果:\n");
                for (Map.Entry<String, Integer> entry : savedCounts.entrySet()) {
                    resultMessage.append(String.format("服务器 %s: 保存 %d 条记录\n", 
                            entry.getKey(), entry.getValue()));
                }
                
                // 清空下载文件夹
                int deletedFiles = cleanDownloadFolder();
                resultMessage.append(String.format("\n已清空下载文件夹，删除 %d 个HTML文件", deletedFiles));
            }
            
            log.info("======== 全部服务器数据解析完成 ========");
            return resultMessage.toString();
        } catch (Exception e) {
            log.error("解析过程中发生错误: {}", e.getMessage(), e);
            return "解析错误: " + e.getMessage();
        }
    }
    
    /**
     * 将已下载的HTML文件解析并保存到数据库
     */
    @ShellMethod(key = {"8", "save-to-db"}, value = "将已解析的数据保存到数据库")
    public String saveToDatabase() {
        log.info("======== 开始将数据保存到数据库 ========");
        
        try {
            List<Map<String, Object>> allResults = SimpleHtmlParser.parseAllServers();
            
            log.info("成功解析服务器数量: {}", allResults.size());
            Map<String, Integer> savedCounts = historyDataService.saveAllServersData(allResults);
            
            StringBuilder result = new StringBuilder("数据库保存结果:\n");
            int totalSaved = 0;
            
            for (Map.Entry<String, Integer> entry : savedCounts.entrySet()) {
                result.append(String.format("服务器 %s: 保存 %d 条记录\n", 
                        entry.getKey(), entry.getValue()));
                totalSaved += entry.getValue();
            }
            
            result.append(String.format("\n总计保存记录数: %d", totalSaved));
            
            // 清空下载文件夹
            int deletedFiles = cleanDownloadFolder();
            result.append(String.format("\n\n已清空下载文件夹，删除 %d 个HTML文件", deletedFiles));
            
            log.info("======== 数据库保存完成 ========");
            return result.toString();
        } catch (Exception e) {
            log.error("保存数据库过程中发生错误: {}", e.getMessage(), e);
            return "保存错误: " + e.getMessage();
        }
    }
}