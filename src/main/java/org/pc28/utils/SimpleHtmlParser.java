package org.pc28.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pc28.constants.WebConstants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 简单HTML解析器 - 直接解析HTML文件内容
 */
public class SimpleHtmlParser {

    /**
     * 解析开奖结果和历史记录
     * 
     * @param htmlFile HTML文件路径
     * @return 解析结果
     */
    public static Map<String, Object> parseHtml(String htmlFile) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            File input = new File(htmlFile);
            Document doc = Jsoup.parse(input, "UTF-8");
            
            // 1. 解析当前期开奖信息
            parsePrimaryResultInfo(doc, result);
            
            // 2. 解析历史记录
            parseHistoryResults(doc, result);
            
            System.out.println("===== 解析完成 =====");
            return result;
        } catch (IOException e) {
            System.err.println("解析HTML文件时出错: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 解析主要开奖信息
     */
    private static void parsePrimaryResultInfo(Document doc, Map<String, Object> result) {
        // 提取期号
        Element periodElement = doc.selectFirst(".period");
        if (periodElement != null) {
            result.put("period", periodElement.text().trim());
            System.out.println("期号: " + periodElement.text().trim());
        }
        
        // 提取开奖倒计时
        Element timesElement = doc.getElementById("times");
        if (timesElement != null) {
            Elements timeValues = timesElement.select("b");
            if (timeValues.size() >= 2) {
                String minutes = timeValues.get(0).text().trim();
                String seconds = timeValues.get(1).text().trim();
                String countdown = minutes + "分" + seconds + "秒";
                result.put("countdown_minutes", minutes);
                result.put("countdown_seconds", seconds);
                result.put("countdown", countdown);
                System.out.println("距离开奖时间: " + countdown);
            }
        }
        
        // 提取开奖号码
        Element numberElement = doc.selectFirst(".number");
        if (numberElement != null) {
            result.put("draw_numbers", numberElement.text().trim());
            System.out.println("开奖号码: " + numberElement.text().trim());
        }
        
        // 提取开奖结果（大/小/单/双）
        Element resultElement = doc.selectFirst(".result-dxds");
        if (resultElement != null) {
            result.put("draw_result", resultElement.text().trim());
            System.out.println("开奖结果: " + resultElement.text().trim());
        }
        
        // 提取未开信息
        Elements notOpenElements = doc.select(".not-open p");
        if (notOpenElements != null && !notOpenElements.isEmpty()) {
            for (Element e : notOpenElements) {
                String id = e.id();
                String count = e.text().trim();
                if (id != null && !id.isEmpty()) {
                    result.put("not_open_" + id.toLowerCase(), count);
                    String type = "";
                    switch (id) {
                        case "DASHUANG": type = "大双"; break;
                        case "DADAN": type = "大单"; break;
                        case "XIAOSHUANG": type = "小双"; break;
                        case "XIAODA": type = "小单"; break;
                        default: type = id;
                    }
                    System.out.println(type + "未开: " + count + " 期");
                }
            }
        }
    }
    
    /**
     * 解析历史记录表格
     */
    private static void parseHistoryResults(Document doc, Map<String, Object> result) {
        Element forecastTable = doc.selectFirst("div.result-table#forecast");
        if (forecastTable == null) {
            System.out.println("未找到历史记录表格");
            return;
        }
        
        Elements historyRows = forecastTable.select(".result-tr");
        System.out.println("解析历史记录，共 " + historyRows.size() + " 条");
        
        int count = 0;
        for (Element row : historyRows) {
            Elements spans = row.select("span");
            if (spans.size() < 4) continue;
            
            // 提取期号
            String periodStr = spans.get(0).text().trim();
            try {
                // 将期号转换为整数
                Integer periodInt = Integer.parseInt(periodStr);
                result.put("history_" + count + "_period", periodInt);
            } catch (NumberFormatException e) {
                // 如果转换失败，仍然保存原始字符串
                result.put("history_" + count + "_period", periodStr);
            }
            
            // 提取号码
            String numbers = spans.get(1).text().trim();
            result.put("history_" + count + "_numbers", numbers);
            
            // 提取预测结果
            Element predictionSpan = spans.get(2);
//            System.out.println("预测结果: " + predictionSpan.text().trim());
            StringBuilder prediction = new StringBuilder();
            Elements predElements = predictionSpan.select("span");
            for (Element predElement : predElements) {
                String predText = predElement.text().trim();
                boolean isMatch = predElement.hasClass("yc-yes");
                prediction.append(predText).append(isMatch ? "(√)" : "(×)").append(" ");
            }
            String predictionStr = prediction.toString().trim();
            
            // 提取预测类型（单或双）
            String predictionType = "";
            if (predictionStr.contains("单")) {
                predictionType = "单";
            } else if (predictionStr.contains("双")) {
                predictionType = "双";
            }
            
            // 保存简化后的预测结果（单或双）
            result.put("history_" + count + "_prediction", predictionType);
            
            // 提取结果（中/错） - 使用spans[5]获取中/错
            Element outcomeSpan = spans.get(5);
            Elements outcomeElements = outcomeSpan.select("span");
            String outcomeText = outcomeSpan.text().trim();
            
            // 从号码中提取总数并转换为整数
            String totalNumberStr = NumberAnalysisUtil.extractTotalNumber(numbers);
            try {
                Integer totalNumberInt = Integer.parseInt(totalNumberStr);
                result.put("history_" + count + "_total_number", totalNumberInt);
            } catch (NumberFormatException e) {
                // 如果转换失败，保存原始字符串
                result.put("history_" + count + "_total_number", totalNumberStr);
            }
            
            // 检查结果并移除调试输出
            String outcome;
            if (outcomeText.contains("中")) {
                outcome = "中";
                result.put("history_" + count + "_is_hit", true);
            } else if (outcomeText.contains("错")) {
                outcome = "错";
                result.put("history_" + count + "_is_hit", false);
            } else if (!outcomeElements.isEmpty()) {
                Element innerSpan = outcomeElements.first();
                if (innerSpan.hasClass("yc-yes2")) {
                    outcome = "中";
                    result.put("history_" + count + "_is_hit", true);
                } else if (innerSpan.hasClass("yc-no2")) {
                    outcome = "错";
                    result.put("history_" + count + "_is_hit", false);
                } else {
                    outcome = "---";
                    result.put("history_" + count + "_is_hit", false);
                }
            } else {
                outcome = "---";
                result.put("history_" + count + "_is_hit", false);
            }
            
            result.put("history_" + count + "_outcome", outcome);
            
            // 输出解析结果
            // System.out.println(
            //     "期号: " + periodStr + 
            //     ", 号码: " + numbers + 
            //     ", 原始预测: " + predictionStr + 
            //     ", 简化预测: " + predictionType + 
            //     ", 结果: " + outcome
            // );
            
            count++;
        }
        
        result.put("history_count", count);
        System.out.println("共解析 " + count + " 条历史记录");
    }
    
    /**
     * 解析所有服务器的最新HTML文件
     * 
     * @return 每个服务器的解析结果列表
     */
    public static List<Map<String, Object>> parseAllServers() {
        List<Map<String, Object>> allResults = new ArrayList<>();
        
        System.out.println("===== 开始解析所有服务器数据 =====");
        for (int i = 0; i < WebConstants.TARGET_URLS.length; i++) {
            // 从URL中提取服务器编号
            String url = WebConstants.TARGET_URLS[i];
            // 提取形如sf1, sf6等服务器编号
            String serverPrefix = url.contains("_sf") ? "sf" + url.split("_sf")[1].split("_")[0] : "sf" + (i + 1);
            
            String latestFile = WebConstants.getLatestHtmlFile(WebConstants.SAVE_PATH, serverPrefix);
            
            if (latestFile != null) {
                System.out.println("\n解析服务器 " + serverPrefix + " 的最新数据文件: " + latestFile);
                Map<String, Object> result = parseHtml(latestFile);
                result.put("server", serverPrefix);
                result.put("file_path", latestFile);
                allResults.add(result);
            } else {
                System.out.println("\n未找到服务器 " + serverPrefix + " 的HTML文件");
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("server", serverPrefix);
                emptyResult.put("error", "文件不存在");
                allResults.add(emptyResult);
            }
        }
        
        System.out.println("\n===== 所有服务器数据解析完成 =====");
        System.out.println("共解析服务器数量: " + allResults.size());
        
        return allResults;
    }
    
    /**
     * 对比所有服务器的当前期开奖结果
     * 
     * @param allResults 所有服务器的解析结果
     */
    public static void compareServerResults(List<Map<String, Object>> allResults) {
        System.out.println("\n===== 各服务器当前期对比 =====");
        
        // 输出标题行
        System.out.println(String.format("%-8s %-10s %-15s %-10s", "服务器", "期号", "开奖号码", "结果"));
        
        for (Map<String, Object> result : allResults) {
            String server = (String) result.get("server");
            
            if (result.containsKey("error")) {
                System.out.println(String.format("%-8s %s", server, "数据不可用"));
                continue;
            }
            
            String period = (String) result.get("period");
            String numbers = (String) result.get("draw_numbers");
            String drawResult = (String) result.get("draw_result");
            
            System.out.println(String.format("%-8s %-10s %-15s %-10s", 
                    server, 
                    (period != null) ? period : "-", 
                    (numbers != null) ? numbers : "-",
                    (drawResult != null) ? drawResult : "-"));
        }
    }
    
    /**
     * 命令行工具入口 - 解析所有服务器数据
     */
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("single")) {
            // 单文件模式
            String htmlFilePath = "e:/pc28/download/redtaotao.html";
            if (args.length > 1) {
                htmlFilePath = args[1];
            }
            parseHtml(htmlFilePath);
        } else {
            // 多服务器模式
            List<Map<String, Object>> allResults = parseAllServers();
            compareServerResults(allResults);
        }
    }
}
