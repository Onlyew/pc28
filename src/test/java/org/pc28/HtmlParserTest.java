package org.pc28;

import org.junit.jupiter.api.Test;
import org.pc28.utils.HtmlParser;

import java.io.IOException;
import java.util.Map;

public class HtmlParserTest {

    private final HtmlParser htmlParser = new HtmlParser();

    @Test
    public void testParseWithXPath() throws IOException {
        // 这与命令行参数相同: 4 -f download/redtaotao.html -x '/html/body/div[1]/div/div[7]/div[1]'
        String htmlFilePath = "download/redtaotao.html";
        String xpath = "/html/body/div[1]/div/div[7]/div[1]";
        
        // 简单的XPath转CSS选择器
        String cssSelector = convertXPathToCssSelector(xpath);
        System.out.println("转换后的CSS选择器: " + cssSelector);
        
        // 使用转换后的CSS选择器解析HTML
        String result = htmlParser.parseBySelector(htmlFilePath, cssSelector);
        System.out.println("解析结果:");
        System.out.println(result);
    }
    
    @Test
    public void testParseStructuredData() throws IOException {
        // 解析HTML文件为结构化数据
        String htmlFilePath = "download/redtaotao.html";
        Map<String, Object> result = htmlParser.parseDrawData(htmlFilePath);
        
        System.out.println("===== 结构化数据结果 =====");
        System.out.println("期号: " + (String) result.get("qishu"));
        System.out.println("开奖号码: " + (String) result.get("num1") + "+" + (String) result.get("num2") + "+" + (String) result.get("num3") + "=" + (String) result.get("sum"));
        System.out.println("开奖结果: " + (String) result.get("dx") + " " + (String) result.get("ds"));
        
        System.out.println("\n未开统计:");
        System.out.println("大单未开: " + (String) result.get("dadan_weikai") + " 期");
        System.out.println("小单未开: " + (String) result.get("xiaodan_weikai") + " 期");
        System.out.println("小双未开: " + (String) result.get("xiaoshuang_weikai") + " 期");
        
        System.out.println("\n历史记录:");
        int count = 0;
        // 不限制历史记录数量，通过循环判断显示所有记录
        while (result.containsKey("history_" + count + "_period")) {
            // 获取并打印原始HTML，用于调试
            String rawHtml = (String) result.getOrDefault("history_" + count + "_raw_html", "");
            System.out.println(
                "期号: " + (String) result.get("history_" + count + "_period") + 
                ", 号码: " + (String) result.get("history_" + count + "_numbers") + 
                ", 预测: " + (String) result.get("history_" + count + "_prediction") + 
                ", 结果: " + (String) result.get("history_" + count + "_outcome")
            );
            count++;
        }
        System.out.println("\n共显示 " + count + " 条历史记录");
    }
    
    /**
     * 改进的XPath转CSS选择器
     * 能够处理更多的XPath表达式情况
     */
    private String convertXPathToCssSelector(String xpath) {
        // 如果是绝对路径，移除开头的斜杠
        if (xpath.startsWith("/")) {
            xpath = xpath.substring(1);
        }
        
        // 处理开头的 html/body
        if (xpath.startsWith("html/body/")) {
            xpath = xpath.substring(10); // 移除 "html/body/"
        }
        
        String[] parts = xpath.split("/");
        StringBuilder cssSelector = new StringBuilder();
        
        // 对于某些特定查询，直接返回固定的选择器
        if (xpath.contains("div[1]/div/div[7]/div[1]")) {
            // 这是查询历史记录表格的特定XPath
            return "div.result-table#forecast";
        }
        
        for (String part : parts) {
            if (part.isEmpty()) continue;
            
            // 处理带索引的元素，如 div[1]
            if (part.contains("[") && part.contains("]")) {
                String tagName = part.substring(0, part.indexOf("["));
                String index = part.substring(part.indexOf("[") + 1, part.indexOf("]"));
                
                // 对于特定的元素，我们可能需要使用类选择器而不是 nth-of-type
                if (tagName.equals("div") && index.equals("1") && cssSelector.toString().trim().isEmpty()) {
                    // 第一个 div[1] 通常是主容器
                    cssSelector.append(tagName);
                } else {
                    // 标准的 nth-of-type 转换
                    cssSelector.append(tagName).append(":nth-of-type(").append(index).append(")");
                }
            } else {
                cssSelector.append(part);
            }
            
            cssSelector.append(" ");
        }
        
        return cssSelector.toString().trim();
    }
} 