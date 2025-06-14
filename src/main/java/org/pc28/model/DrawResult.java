package org.pc28.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 开奖结果数据模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrawResult {
    /**
     * 期号
     */
    private String qishu;
    
    /**
     * 距离开奖时间（分钟）
     */
    private String countdownMinutes;
    
    /**
     * 距离开奖时间（秒）
     */
    private String countdownSeconds;
    
    /**
     * 距离开奖时间（完整格式）
     */
    private String countdown;
    
    /**
     * 第一个数字
     */
    private int num1;
    
    /**
     * 第二个数字
     */
    private int num2;
    
    /**
     * 第三个数字
     */
    private int num3;
    
    /**
     * 和值
     */
    private int sum;
    
    /**
     * 大小结果
     */
    private String dxResult;
    
    /**
     * 单双结果
     */
    private String dsResult;
    
    /**
     * 大单未开期数
     */
    private int dadanWeikai;
    
    /**
     * 小单未开期数
     */
    private int xiaodanWeikai;
    
    /**
     * 小双未开期数
     */
    private int xiaoshuangWeikai;
    
    /**
     * 历史记录列表
     */
    private List<HistoryRecord> historyRecords;
    
    /**
     * 历史记录数据模型
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryRecord {
        /**
         * 期号
         */
        private String period;
        
        /**
         * 开奖号码
         */
        private String numbers;
        
        /**
         * 预测结果
         */
        private String prediction;
        
        /**
         * 实际结果
         */
        private String outcome;
    }
} 