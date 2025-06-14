package org.pc28.utils;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 号码分析工具类
 * 用于分析开奖号码，提取总数和判断是否为"杀"
 */
public class NumberAnalysisUtil {
    
    // 匹配形如 "1+2+3=6" 的号码格式的正则表达式
    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)\\+(\\d+)\\+(\\d+)=(\\d+)");
    
    /**
     * 从号码字符串中提取总数
     * 例如从 "1+2+3=6" 提取出 "6"
     * 
     * @param numbers 号码字符串，例如 "1+2+3=6"
     * @return 总数字符串，如果格式不匹配则返回空字符串
     */
    public static String extractTotalNumber(String numbers) {
        if (numbers == null || numbers.isEmpty() || "---".equals(numbers) || "-".equals(numbers)) {
            return "";
        }
        
        Matcher matcher = NUMBER_PATTERN.matcher(numbers);
        if (matcher.find()) {
            return matcher.group(4);
        }
        
        return "";
    }
    
    /**
     * 判断号码是否为"杀"
     * 判断规则：
     * 1. 总和为13或14
     * 2. 存在对子（两个数字相同）或豹子（三个数字相同）
     * 3. 存在顺子（三个连续的数字，不考虑顺序）
     * 
     * @param numbers 号码字符串，例如 "1+2+3=6"
     * @return 如果是"杀"返回"杀"，否则返回空字符串
     */
    public static String checkIfKill(String numbers) {
        if (numbers == null || numbers.isEmpty() || "---".equals(numbers) || "-".equals(numbers)) {
            return "";
        }
        
        Matcher matcher = NUMBER_PATTERN.matcher(numbers);
        if (!matcher.find()) {
            return "";
        }
        
        // 提取三个数字和总和
        int num1 = Integer.parseInt(matcher.group(1));
        int num2 = Integer.parseInt(matcher.group(2));
        int num3 = Integer.parseInt(matcher.group(3));
        int total = Integer.parseInt(matcher.group(4));
        
        // 判断总和是否为13或14
        if (total == 13 || total == 14) {
            return "杀";
        }
        
        // 判断是否有对子或豹子
        if (num1 == num2 || num1 == num3 || num2 == num3) {
            return "杀";
        }
        
        // 判断是否有顺子（三个连续的数字）
        int[] nums = new int[]{num1, num2, num3};
        Arrays.sort(nums);
        if (nums[1] == nums[0] + 1 && nums[2] == nums[1] + 1) {
            return "杀";
        }
        
        return "";
    }
    
    /**
     * 分析号码并一次性返回总数和杀标记
     * 
     * @param numbers 号码字符串，例如 "1+2+3=6"
     * @return 长度为2的字符串数组，第一个元素是总数，第二个元素是杀标记（"杀"或空字符串）
     */
    public static String[] analyzeNumbers(String numbers) {
        String totalNumber = extractTotalNumber(numbers);
        String killMark = checkIfKill(numbers);
        return new String[]{totalNumber, killMark};
    }
    
    /**
     * 判断总数是单数还是双数
     * 
     * @param totalNumber 总数字符串，例如 "13"
     * @return 如果是单数返回“单”，双数返回“双”，如果不能判断则返回空字符串
     */
    public static String determineOddEven(String totalNumber) {
        if (totalNumber == null || totalNumber.isEmpty()) {
            return "";
        }
        
        try {
            int num = Integer.parseInt(totalNumber);
            return (num % 2 == 0) ? "双" : "单";
        } catch (NumberFormatException e) {
            // 如果无法解析为数字
            return "";
        }
    }
}
