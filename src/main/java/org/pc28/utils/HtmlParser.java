package org.pc28.utils;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pc28.model.DrawResult;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTML解析工具类，用于解析网页数据
 */
@Slf4j
public class HtmlParser {

    /**
     * 从HTML文件中解析开奖数据并返回结构化的对象
     *
     * @param htmlFilePath HTML文件路径
     * @return 结构化的开奖结果对象
     * @throws IOException 如果读取文件出错
     */
    public DrawResult parseDrawResultObject(String htmlFilePath) throws IOException {
        log.info("开始解析HTML文件到结构化对象: {}", htmlFilePath);
        
        File input = new File(htmlFilePath);
        Document doc = Jsoup.parse(input, "UTF-8");
        
        try {
            DrawResult.DrawResultBuilder builder = DrawResult.builder();
            
            // 解析期号
            Element qishuElement = doc.getElementById("qishu");
            if (qishuElement != null) {
                String qishu = qishuElement.text().trim();
                builder.qishu(qishu);
                log.info("期号: {}", qishu);
            }
            
            // 解析开奖号码
            String num1Text = doc.getElementById("mi-num1").text();
            String num2Text = doc.getElementById("mi-num2").text();
            String num3Text = doc.getElementById("mi-num3").text();
            String sumText = doc.getElementById("mi-num4").text();
            
            int num1 = Integer.parseInt(num1Text);
            int num2 = Integer.parseInt(num2Text);
            int num3 = Integer.parseInt(num3Text);
            int sum = Integer.parseInt(sumText);
            
            builder.num1(num1)
                   .num2(num2)
                   .num3(num3)
                   .sum(sum);
            
            log.info("开奖号码: {}+{}+{}={}", num1, num2, num3, sum);
            
            // 解析大小单双结果
            String dxResult = doc.getElementById("mi-dx").text();
            String dsResult = doc.getElementById("mi-ds").text();
            builder.dxResult(dxResult)
                   .dsResult(dsResult);
            
            log.info("开奖结果: {} {}", dxResult, dsResult);
            
            // 解析未开统计
            Element dadanElement = doc.getElementById("DADAN");
            Element xiaodanElement = doc.getElementById("XIAODAN");
            Element xiaoshuangElement = doc.getElementById("XIAOSHUANG");
            
            if (dadanElement != null) {
                int dadanWeikai = Integer.parseInt(dadanElement.text());
                builder.dadanWeikai(dadanWeikai);
                log.info("大单未开: {} 期", dadanWeikai);
            }
            
            if (xiaodanElement != null) {
                int xiaodanWeikai = Integer.parseInt(xiaodanElement.text());
                builder.xiaodanWeikai(xiaodanWeikai);
                log.info("小单未开: {} 期", xiaodanWeikai);
            }
            
            if (xiaoshuangElement != null) {
                int xiaoshuangWeikai = Integer.parseInt(xiaoshuangElement.text());
                builder.xiaoshuangWeikai(xiaoshuangWeikai);
                log.info("小双未开: {} 期", xiaoshuangWeikai);
            }
            
            // 解析历史结果
            Elements resultTrElements = doc.select("div.result-tr");
            int historyCount = Math.min(20, resultTrElements.size()); // 最多取20条历史记录
            
            List<DrawResult.HistoryRecord> historyRecords = new ArrayList<>();
            log.info("解析历史记录，共 {} 条", historyCount);
            
            for (int i = 0; i < historyCount; i++) {
                Element trElement = resultTrElements.get(i);
                Elements spans = trElement.select("span");
                if (spans.size() >= 4) {
                    String period = spans.get(0).text(); // 期号
                    String numbers = spans.get(1).text(); // 号码
                    String prediction = spans.get(2).text(); // 预测
                    String outcome = spans.get(3).text(); // 结果
                    
                    DrawResult.HistoryRecord record = DrawResult.HistoryRecord.builder()
                            .period(period)
                            .numbers(numbers)
                            .prediction(prediction)
                            .outcome(outcome)
                            .build();
                    
                    historyRecords.add(record);
                    log.debug("历史记录 {}: 期号={}, 号码={}, 预测={}, 结果={}", 
                            i, period, numbers, prediction, outcome);
                }
            }
            
            builder.historyRecords(historyRecords);
            DrawResult result = builder.build();
            log.info("HTML文件解析为结构化对象完成");
            return result;
            
        } catch (Exception e) {
            log.error("解析HTML文件到结构化对象时发生错误: {}", e.getMessage(), e);
            throw new IOException("解析错误: " + e.getMessage(), e);
        }
    }

    /**
     * 从HTML文件中解析开奖数据
     *
     * @param htmlFilePath HTML文件路径
     * @return 解析结果，包含期号、号码、开奖结果等
     * @throws IOException 如果读取文件出错
     */
    public Map<String, Object> parseDrawData(String htmlFilePath) throws IOException {
        log.info("开始解析HTML文件: {}", htmlFilePath);
        Map<String, Object> result = new HashMap<>();
        
        File input = new File(htmlFilePath);
        Document doc = Jsoup.parse(input, "UTF-8");
        
        try {
            // 解析期号
            Element qishuElement = doc.getElementById("qishu");
            if (qishuElement != null) {
                String qishu = qishuElement.text().trim();
                result.put("qishu", qishu);
                log.info("期号: {}", qishu);
            }
            
            // 解析开奖倒计时
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
                    log.info("距离开奖时间: {}", countdown);
                }
            }
            
            // 解析开奖号码
            String num1 = doc.getElementById("mi-num1").text();
            String num2 = doc.getElementById("mi-num2").text();
            String num3 = doc.getElementById("mi-num3").text();
            String sum = doc.getElementById("mi-num4").text();
            result.put("num1", num1);
            result.put("num2", num2);
            result.put("num3", num3);
            result.put("sum", sum);
            log.info("开奖号码: {}+{}+{}={}", num1, num2, num3, sum);
            
            // 解析大小单双结果
            String dxResult = doc.getElementById("mi-dx").text();
            String dsResult = doc.getElementById("mi-ds").text();
            result.put("dx", dxResult); // 大小
            result.put("ds", dsResult); // 单双
            log.info("开奖结果: {} {}", dxResult, dsResult);
            
            // 解析未开统计
            Element dadanElement = doc.getElementById("DADAN");
            Element xiaodanElement = doc.getElementById("XIAODAN");
            Element xiaoshuangElement = doc.getElementById("XIAOSHUANG");
            
            if (dadanElement != null) {
                result.put("dadan_weikai", dadanElement.text());
                log.info("大单未开: {} 期", dadanElement.text());
            }
            
            if (xiaodanElement != null) {
                result.put("xiaodan_weikai", xiaodanElement.text());
                log.info("小单未开: {} 期", xiaodanElement.text());
            }
            
            if (xiaoshuangElement != null) {
                result.put("xiaoshuang_weikai", xiaoshuangElement.text());
                log.info("小双未开: {} 期", xiaoshuangElement.text());
            }
            
            // 解析历史结果
            Elements resultTrElements = doc.select("div.result-tr");
            int historyCount = resultTrElements.size(); // 显示所有历史记录
            
            log.info("解析历史记录，共 {} 条", historyCount);
            for (int i = 0; i < historyCount; i++) {
                Element trElement = resultTrElements.get(i);
                Elements spans = trElement.select("span");
                if (spans.size() >= 4) {
                    // 期号
                    String period = spans.get(0).text();
                    result.put("history_" + i + "_period", period);
                    
                    // 号码 - 分别提取各个数字和和值
                    Element numbersSpan = spans.get(1);
                    List<String> codeItems = new ArrayList<>();
                    Elements codeItemElements = numbersSpan.select("em.code-item");
                    for (Element em : codeItemElements) {
                        codeItems.add(em.text());
                    }
                    
                    String histNum1 = codeItems.size() > 0 ? codeItems.get(0) : "";
                    String histNum2 = codeItems.size() > 1 ? codeItems.get(1) : "";
                    String histNum3 = codeItems.size() > 2 ? codeItems.get(2) : "";
                    String histSum = numbersSpan.select("em.code-sum").text();
                    
                    // 构建号码字符串，例如: "5+1+5=11"
                    StringBuilder numbersStr = new StringBuilder();
                    if (!codeItems.isEmpty()) {
                        numbersStr.append(String.join("+", codeItems));
                        if (!histSum.isEmpty()) {
                            numbersStr.append("=").append(histSum);
                        }
                    } else {
                        numbersStr.append(numbersSpan.text()); // 如果无法分析，则使用整个文本
                    }
                    
                    result.put("history_" + i + "_numbers", numbersStr.toString());
                    result.put("history_" + i + "_num1", histNum1);
                    result.put("history_" + i + "_num2", histNum2);
                    result.put("history_" + i + "_num3", histNum3);
                    result.put("history_" + i + "_sum", histSum);
                    
                    // 预测 - 考虑 yc-yes/yc-no 的语义
                    Element predictionSpan = spans.get(2);
                    StringBuilder prediction = new StringBuilder();
                    Elements predictionElements = predictionSpan.select("span.yc-yes, span.yc-no");
                    
                    for (Element predElement : predictionElements) {
                        String predText = predElement.text();
                        boolean isMatch = predElement.hasClass("yc-yes");
                        prediction.append(predText).append(isMatch ? "(√)" : "(×)").append(" ");
                    }
                    
                    result.put("history_" + i + "_prediction", prediction.toString().trim());
                    
                    // 结果 - 检查是否"中"或"错"
                    Element outcomeSpan = spans.get(3);
                    String outcomeHtml = outcomeSpan.outerHtml();  // 获取完整的HTML包括标签
                    String outcomeText = outcomeSpan.text().trim(); // 获取纯文本内容
                    
                    // 直接正对解析能看到的HTML结构：<span><span class="yc-yes2">中</span></span>
                    // 尝试多种方式获取结果元素
                    Element directYesElement = outcomeSpan.select(".yc-yes2").first();
                    Element directNoElement = outcomeSpan.select(".yc-no2").first();
                    
                    // 可能的颜色样式包裹，比如 <span style="color: #00a0e9"><span class="yc-yes2">中</span></span>
                    boolean hasYes = directYesElement != null || 
                                    outcomeHtml.contains("yc-yes2") || 
                                    outcomeText.contains("中");
                    boolean hasNo = directNoElement != null || 
                                   outcomeHtml.contains("yc-no2") || 
                                   outcomeText.contains("错");
                    
                    // 打印详细日志以便调试
                    log.info("期号: {}, 结果解析: directYes={}, directNo={}, hasYes={}, hasNo={}, HTML:{}", 
                            period, directYesElement != null, directNoElement != null, hasYes, hasNo, outcomeHtml);
                    
                    // 根据分析结果做决定
                    String outcome;
                    if (hasYes) {
                        outcome = "中";
                    } else if (hasNo) {
                        outcome = "错";
                    } else {
                        outcome = "---";  // 未开奖或其他状态
                    }
                    
                    // 将结果正确存入结果集
                    result.put("history_" + i + "_outcome", outcome);
                    result.put("history_" + i + "_is_hit", hasYes);
                    result.put("history_" + i + "_raw_html", outcomeHtml);  // 存储原始HTML便于调试
                    
                    // 额外输出纯文本也存储一份，便于排查
                    result.put("history_" + i + "_outcome_text", outcomeSpan.text());
                    
                    log.debug("历史记录 {}: 期号={}, 号码={}, 预测={}, 结果={}", 
                            i, period, numbersStr, prediction, outcome);
                }
            }
            
        } catch (Exception e) {
            log.error("解析HTML文件时发生错误: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
        }
        
        log.info("HTML文件解析完成");
        return result;
    }
    
    /**
     * 从HTML文件中解析并输出指定XPath的元素内容
     *
     * @param htmlFilePath HTML文件路径
     * @param cssSelector CSS选择器
     * @return 选择器匹配的元素内容
     * @throws IOException 如果读取文件出错
     */
    public String parseBySelector(String htmlFilePath, String cssSelector) throws IOException {
        log.info("开始解析HTML文件: {}, 选择器: {}", htmlFilePath, cssSelector);
        StringBuilder result = new StringBuilder();
        
        File input = new File(htmlFilePath);
        Document doc = Jsoup.parse(input, "UTF-8");
        
        try {
            Elements elements = doc.select(cssSelector);
            if (elements.isEmpty()) {
                log.warn("未找到匹配选择器的元素: {}", cssSelector);
                return "未找到匹配选择器的元素";
            }
            
            for (Element element : elements) {
                result.append(element.outerHtml()).append("\n\n");
            }
            
            log.info("找到 {} 个匹配元素", elements.size());
        } catch (Exception e) {
            log.error("解析HTML文件时发生错误: {}", e.getMessage(), e);
            return "解析错误: " + e.getMessage();
        }
        
        return result.toString();
    }
} 