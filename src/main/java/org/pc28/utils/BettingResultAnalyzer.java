package org.pc28.utils;

/**
 * 下注结果分析工具类
 * 用于分析预测结果和实际结果的对比，判断下注后的盈亏情况
 */
public class BettingResultAnalyzer {

    /**
     * 下注结果枚举
     */
    public enum BettingResult {
        /**
         * 命中盈利：预判正确 + 开中 + 没有杀
         */
        HIT_PROFIT("命中盈利"),
        
        /**
         * 命中被杀：预判正确 + 开中 + 有杀
         */
        HIT_KILLED("命中被杀"),
        
        /**
         * 未命中盈利：预判错误(outcome为"错") + 没有杀
         */
        MISS_PROFIT("未命中盈利"),
        
        /**
         * 未命中亏损：预判错误(outcome为"错") + 有杀
         */
        MISS_LOSS("未命中亏损"),
        
        /**
         * 无法判断：数据不完整或格式错误
         */
        UNKNOWN("未知");
        
        private final String description;
        
        BettingResult(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 分析下注结果
     *
     * @param prediction 预测结果，例如：“单”、“双”
     * @param outcome 实际结果，“中”或“错”
     * @param hasKillNumber 是否有杀标记
     * @return 下注结果枚举
     */
    public static BettingResult analyzeBettingResult(String prediction, String outcome, boolean hasKillNumber) {
        // 检查参数有效性
        if (prediction == null || outcome == null || prediction.isEmpty()) {
            return BettingResult.UNKNOWN;
        }
        
        // 判断下注结果
        if (outcome.equals("中")) {
            return hasKillNumber ? BettingResult.HIT_KILLED : BettingResult.HIT_PROFIT;
        } else if (outcome.equals("错")) {
            return hasKillNumber ? BettingResult.MISS_LOSS : BettingResult.MISS_PROFIT;
        }
        
        return BettingResult.UNKNOWN;
    }
    
    /**
     * 计算指定场次内的胜率
     *
     * @param results 下注结果数组
     * @param numGames 要计算的场次数，如10、20、30等
     * @return 胜率（0-1之间的小数）
     */
    public static double calculateWinRate(BettingResult[] results, int numGames) {
        if (results == null || results.length == 0 || numGames <= 0) {
            return 0.0;
        }
        
        // 计算实际要分析的场次数（避免数组越界）
        int actualGames = Math.min(numGames, results.length);
        
        // 统计胜利场次（命中盈利和未命中盈利都算作胜利）
        int wins = 0;
        for (int i = 0; i < actualGames; i++) {
            if (results[i] == BettingResult.HIT_PROFIT || results[i] == BettingResult.MISS_PROFIT) {
                wins++;
            }
        }
        
        // 返回胜率
        return (double) wins / actualGames;
    }
    
    /**
     * 从历史记录中分析下注结果
     *
     * @param prediction 预测结果字符串，例如“单(×) 单(√)”或“双(×) 双(×)”
     * @param outcome 结果，“中”或“错”
     * @param killNumber 是否有杀标记
     * @return 下注结果
     */
    public static BettingResult analyzeFromHistoryRecord(String prediction, String outcome, String killNumber) {
        // 提取预测类型（单或双）
        String predictionType = extractPredictionType(prediction);
        boolean hasKill = killNumber != null && killNumber.equals("杀");
        
        return analyzeBettingResult(predictionType, outcome, hasKill);
    }
    
    /**
     * 从预测字符串中提取预测类型（单或双）
     *
     * @param prediction 预测字符串，例如“单(×) 单(√)”或“双(×) 双(×)”
     * @return 预测类型，如“单”或“双”
     */
    private static String extractPredictionType(String prediction) {
        if (prediction == null || prediction.isEmpty()) {
            return "";
        }
        
        // 首先检查是否包含“单”
        if (prediction.contains("单")) {
            return "单";
        }
        
        // 如果不是“单”，检查是否是“双”
        if (prediction.contains("双")) {
            return "双";
        }
        
        return "";
    }
    
    /**
     * 批量计算多个不同场次的胜率
     *
     * @param results 下注结果数组
     * @param gameRanges 要计算的场次范围数组，如[10, 20, 30, 50, 100]
     * @return 各场次范围的胜率数组
     */
    public static double[] calculateWinRatesForRanges(BettingResult[] results, int[] gameRanges) {
        if (results == null || gameRanges == null) {
            return new double[0];
        }
        
        double[] winRates = new double[gameRanges.length];
        for (int i = 0; i < gameRanges.length; i++) {
            winRates[i] = calculateWinRate(results, gameRanges[i]);
        }
        
        return winRates;
    }
}
