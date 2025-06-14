package org.pc28.service.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pc28.entity.*;
import org.pc28.utils.NumberAnalysisUtil;
import org.pc28.utils.BettingResultAnalyzer;
import org.pc28.utils.BettingResultAnalyzer.BettingResult;
import org.pc28.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 历史数据数据库服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HistoryDataServiceImpl implements HistoryDataService {

    private final Sf1HistoryRepository sf1Repository;
    private final Sf3HistoryRepository sf3Repository;
    private final Sf4HistoryRepository sf4Repository;
    private final Sf5HistoryRepository sf5Repository;
    private final Sf6HistoryRepository sf6Repository;
    private final Sf7HistoryRepository sf7Repository;
    
    @Override
    @Transactional
    public int saveServerData(String serverPrefix, Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            log.warn("没有数据需要保存，服务器前缀: {}", serverPrefix);
            return 0;
        }
        
        if (data.containsKey("error")) {
            log.error("解析数据存在错误，无法保存。服务器前缀: {}, 错误: {}", 
                    serverPrefix, data.get("error"));
            return 0;
        }
        
        log.info("开始保存服务器 {} 的解析数据", serverPrefix);
        
        // 获取历史记录数量
        Integer historyCount = (Integer) data.get("history_count");
        if (historyCount == null || historyCount <= 0) {
            log.warn("没有找到历史记录数据，服务器前缀: {}", serverPrefix);
            return 0;
        }
        
        int savedCount = 0;
        
        // 遍历历史记录并保存
        for (int i = 0; i < historyCount; i++) {
            String periodKey = "history_" + i + "_period";
            String numbersKey = "history_" + i + "_numbers";
            String predictionKey = "history_" + i + "_prediction";
            String outcomeKey = "history_" + i + "_outcome";
            
            if (data.containsKey(periodKey) && data.containsKey(numbersKey)) {
                // 获取期数，可能是Integer或String类型
                Object periodObj = data.get(periodKey);
                String period = periodObj instanceof Integer ? String.valueOf(periodObj) : (String) periodObj;
                String numbers = (String) data.get(numbersKey);
                String prediction = (String) data.getOrDefault(predictionKey, "");
                String outcome = (String) data.getOrDefault(outcomeKey, "");
                
                // 获取总数（可能是Integer或String类型）
                String totalNumberKey = "history_" + i + "_total_number";
                Object totalNumberObj = data.getOrDefault(totalNumberKey, null);
                String totalNumber;
                if (totalNumberObj == null) {
                    // 如果解析器没有提取总数，手动提取
                    totalNumber = NumberAnalysisUtil.extractTotalNumber(numbers);
                } else {
                    // 将总数转换为字符串
                    totalNumber = totalNumberObj instanceof Integer ? String.valueOf(totalNumberObj) : (String) totalNumberObj;
                }
                
                // 提取杀号
                String killNumber = NumberAnalysisUtil.checkIfKill(numbers);
                
                // 记录日志
                if (!killNumber.isEmpty()) {
                    log.debug("检测到杀: {}, 号码: {}", period, numbers);
                }
                
                // 保存历史记录
                boolean saved = saveHistoryRecord(serverPrefix, period, numbers, prediction, outcome, totalNumber, killNumber);
                if (saved) {
                    savedCount++;
                }
            }
        }
        
        log.info("保存服务器 {} 的数据完成，共保存 {} 条历史记录", serverPrefix, savedCount);
        return savedCount;
    }
    
    @Override
    @Transactional
    public Map<String, Integer> saveAllServersData(List<Map<String, Object>> allResults) {
        Map<String, Integer> savedCounts = new HashMap<>();
        
        if (allResults == null || allResults.isEmpty()) {
            log.warn("没有数据需要保存");
            return savedCounts;
        }
        
        log.info("开始保存所有服务器的解析数据...");
        
        for (Map<String, Object> result : allResults) {
            String server = (String) result.get("server");
            if (server == null) {
                log.warn("跳过无服务器标识的数据");
                continue;
            }
            
            int count = saveServerData(server, result);
            savedCounts.put(server, count);
        }
        
        log.info("所有服务器数据保存完成");
        return savedCounts;
    }
    
    /**
     * 根据服务器前缀保存对应的历史记录
     */
    private boolean saveHistoryRecord(String serverPrefix, String period, String numbers, 
                                      String prediction, String outcome, String totalNumber, String killNumber) {
        try {
            switch (serverPrefix) {
                case "sf1":
                    // 将period转换为Integer类型
                    Integer periodInt;
                    try {
                        periodInt = Integer.parseInt(period);
                    } catch (NumberFormatException e) {
                        log.error("期号转换为整数失败: {}", period);
                        return false;
                    }
                    
                    // 检查记录是否存在
                    if (sf1Repository.existsByPeriod(periodInt)) {
                        // 找到存在的记录
                        Sf1HistoryRecord existingRecord = sf1Repository.findByPeriod(periodInt).orElse(null);
                        
                        // 如果记录存在且号码不是"---"，则跳过
                        if (existingRecord != null && !"---".equals(existingRecord.getNumbers()) && !"-".equals(existingRecord.getNumbers())) {
                            log.debug("记录已存在且有效，跳过: {}, period: {}", serverPrefix, period);
                            return false;
                        }
                        
                        // 如果记录存在但号码是"---"，则更新
                        if (existingRecord != null) {
                            log.info("发现无效记录，进行更新: {}, period: {}, 旧号码: {}, 新号码: {}", 
                                   serverPrefix, period, existingRecord.getNumbers(), numbers);
                            
                            existingRecord.setNumbers(numbers);
                            existingRecord.setPrediction(prediction);
                            existingRecord.setOutcome(outcome);
                            try {
                                Integer totalNumberInt = Integer.parseInt(totalNumber);
                                existingRecord.setTotalNumber(totalNumberInt);
                            } catch (NumberFormatException e) {
                                existingRecord.setTotalNumber(null);
                            }
                            existingRecord.setKillNumber(killNumber);
                            
                            // 分析下注结果
                            BettingResult bettingResult = BettingResultAnalyzer.analyzeFromHistoryRecord(
                                prediction, outcome, killNumber);
                            existingRecord.setBettingResult(bettingResult.getDescription());
                            
                            // 设置开奖结果（单或双）
                            String openResult = NumberAnalysisUtil.determineOddEven(totalNumber);
                            existingRecord.setOpenResult(openResult);
                            
                            sf1Repository.save(existingRecord);
                            return true;
                        }
                    }
                    
                    // 创建新记录
                    Sf1HistoryRecord sf1Record = new Sf1HistoryRecord();
                    try {
                        Integer periodInt1 = Integer.parseInt(period);
                        sf1Record.setPeriod(periodInt1);
                    } catch (NumberFormatException e) {
                        sf1Record.setPeriod(null);
                    }
                    sf1Record.setNumbers(numbers);
                    sf1Record.setPrediction(prediction);
                    sf1Record.setOutcome(outcome);
                    
                    // 将totalNumber转换为Integer类型
                    try {
                        Integer totalNumberInt = Integer.parseInt(totalNumber);
                        sf1Record.setTotalNumber(totalNumberInt);
                    } catch (NumberFormatException e) {
                        // 如果无法转换，设置为null
                        sf1Record.setTotalNumber(null);
                    }
                    
                    sf1Record.setKillNumber(killNumber);
                    
                    // 分析下注结果
                    BettingResult sf1NewBettingResult = BettingResultAnalyzer.analyzeFromHistoryRecord(
                        prediction, outcome, killNumber);
                    sf1Record.setBettingResult(sf1NewBettingResult.getDescription());
                    
                    // 设置开奖结果（单或双）
                    String openResult = NumberAnalysisUtil.determineOddEven(totalNumber);
                    sf1Record.setOpenResult(openResult);
                    
                    sf1Repository.save(sf1Record);
                    return true;
                    
                case "sf3":
                    // 将period转换为Integer类型
                    Integer periodInt3;
                    try {
                        periodInt3 = Integer.parseInt(period);
                    } catch (NumberFormatException e) {
                        log.error("期号转换为整数失败: {}", period);
                        return false;
                    }
                    
                    // 检查记录是否存在
                    if (sf3Repository.existsByPeriod(periodInt3)) {
                        // 找到存在的记录
                        Sf3HistoryRecord existingRecord = sf3Repository.findByPeriod(periodInt3).orElse(null);
                        
                        // 如果记录存在且号码不是"---"，则跳过
                        if (existingRecord != null && !"---".equals(existingRecord.getNumbers()) && !"-".equals(existingRecord.getNumbers())) {
                            log.debug("记录已存在且有效，跳过: {}, period: {}", serverPrefix, period);
                            return false;
                        }
                        
                        // 如果记录存在但号码是"---"，则更新
                        if (existingRecord != null) {
                            log.info("发现无效记录，进行更新: {}, period: {}, 旧号码: {}, 新号码: {}", 
                                   serverPrefix, period, existingRecord.getNumbers(), numbers);
                            
                            existingRecord.setNumbers(numbers);
                            existingRecord.setPrediction(prediction);
                            existingRecord.setOutcome(outcome);
                            try {
                                Integer totalNumberInt = Integer.parseInt(totalNumber);
                                existingRecord.setTotalNumber(totalNumberInt);
                            } catch (NumberFormatException e) {
                                existingRecord.setTotalNumber(null);
                            }
                            existingRecord.setKillNumber(killNumber);
                            
                            // 分析下注结果
                            BettingResult bettingResult = BettingResultAnalyzer.analyzeFromHistoryRecord(
                                prediction, outcome, killNumber);
                            existingRecord.setBettingResult(bettingResult.getDescription());
                            
                            // 设置开奖结果（单或双）
                            String sf3ExistingOpenResult = NumberAnalysisUtil.determineOddEven(totalNumber);
                            existingRecord.setOpenResult(sf3ExistingOpenResult);
                            
                            sf3Repository.save(existingRecord);
                            return true;
                        }
                    }
                    
                    // 创建新记录
                    Sf3HistoryRecord sf3Record = new Sf3HistoryRecord();
                    try {
                        Integer periodInt3Record = Integer.parseInt(period);
                        sf3Record.setPeriod(periodInt3Record);
                    } catch (NumberFormatException e) {
                        sf3Record.setPeriod(null);
                    }
                    sf3Record.setNumbers(numbers);
                    sf3Record.setPrediction(prediction);
                    sf3Record.setOutcome(outcome);
                    try {
                        Integer totalNumberInt = Integer.parseInt(totalNumber);
                        sf3Record.setTotalNumber(totalNumberInt);
                    } catch (NumberFormatException e) {
                        sf3Record.setTotalNumber(null);
                    }
                    sf3Record.setKillNumber(killNumber);
                    
                    // 分析下注结果
                    BettingResult sf3BettingResult = BettingResultAnalyzer.analyzeFromHistoryRecord(
                        prediction, outcome, killNumber);
                    sf3Record.setBettingResult(sf3BettingResult.getDescription());
                    
                    // 设置开奖结果（单或双）
                    String sf3OpenResult = NumberAnalysisUtil.determineOddEven(totalNumber);
                    sf3Record.setOpenResult(sf3OpenResult);
                    
                    sf3Repository.save(sf3Record);
                    return true;
                    
                case "sf4":
                    // 将period转换为Integer类型
                    Integer periodInt4;
                    try {
                        periodInt4 = Integer.parseInt(period);
                    } catch (NumberFormatException e) {
                        log.error("期号转换为整数失败: {}", period);
                        return false;
                    }
                    
                    // 检查记录是否存在
                    if (sf4Repository.existsByPeriod(periodInt4)) {
                        // 找到存在的记录
                        Sf4HistoryRecord existingRecord = sf4Repository.findByPeriod(periodInt4).orElse(null);
                        
                        // 如果记录存在且号码不是"---"，则跳过
                        if (existingRecord != null && !"---".equals(existingRecord.getNumbers()) && !"-".equals(existingRecord.getNumbers())) {
                            log.debug("记录已存在且有效，跳过: {}, period: {}", serverPrefix, period);
                            return false;
                        }
                        
                        // 如果记录存在但号码是"---"，则更新
                        if (existingRecord != null) {
                            log.info("发现无效记录，进行更新: {}, period: {}, 旧号码: {}, 新号码: {}", 
                                   serverPrefix, period, existingRecord.getNumbers(), numbers);
                            
                            existingRecord.setNumbers(numbers);
                            existingRecord.setPrediction(prediction);
                            existingRecord.setOutcome(outcome);
                            try {
                                Integer totalNumberInt = Integer.parseInt(totalNumber);
                                existingRecord.setTotalNumber(totalNumberInt);
                            } catch (NumberFormatException e) {
                                existingRecord.setTotalNumber(null);
                            }
                            existingRecord.setKillNumber(killNumber);
                            
                            // 分析下注结果
                            BettingResult bettingResult = BettingResultAnalyzer.analyzeFromHistoryRecord(
                                prediction, outcome, killNumber);
                            existingRecord.setBettingResult(bettingResult.getDescription());
                            
                            // 设置开奖结果（单或双）
                            String sf4ExistingOpenResult = NumberAnalysisUtil.determineOddEven(totalNumber);
                            existingRecord.setOpenResult(sf4ExistingOpenResult);
                            
                            sf4Repository.save(existingRecord);
                            return true;
                        }
                    }
                    
                    // 创建新记录
                    Sf4HistoryRecord sf4Record = new Sf4HistoryRecord();
                    try {
                        Integer periodInt4Record = Integer.parseInt(period);
                        sf4Record.setPeriod(periodInt4Record);
                    } catch (NumberFormatException e) {
                        sf4Record.setPeriod(null);
                    }
                    sf4Record.setNumbers(numbers);
                    sf4Record.setPrediction(prediction);
                    sf4Record.setOutcome(outcome);
                    try {
                        Integer totalNumberInt = Integer.parseInt(totalNumber);
                        sf4Record.setTotalNumber(totalNumberInt);
                    } catch (NumberFormatException e) {
                        sf4Record.setTotalNumber(null);
                    }
                    sf4Record.setKillNumber(killNumber);
                    
                    // 分析下注结果
                    BettingResult sf4BettingResult = BettingResultAnalyzer.analyzeFromHistoryRecord(
                        prediction, outcome, killNumber);
                    sf4Record.setBettingResult(sf4BettingResult.getDescription());
                    
                    // 设置开奖结果（单或双）
                    String sf4OpenResult = NumberAnalysisUtil.determineOddEven(totalNumber);
                    sf4Record.setOpenResult(sf4OpenResult);
                    
                    sf4Repository.save(sf4Record);
                    return true;
                    
                case "sf5":
                    // 将period转换为Integer类型
                    Integer periodInt5;
                    try {
                        periodInt5 = Integer.parseInt(period);
                    } catch (NumberFormatException e) {
                        log.error("期号转换为整数失败: {}", period);
                        return false;
                    }
                    
                    // 检查记录是否存在
                    if (sf5Repository.existsByPeriod(periodInt5)) {
                        // 找到存在的记录
                        Sf5HistoryRecord existingRecord = sf5Repository.findByPeriod(periodInt5).orElse(null);
                        
                        // 如果记录存在且号码不是"---"，则跳过
                        if (existingRecord != null && !"---".equals(existingRecord.getNumbers()) && !"-".equals(existingRecord.getNumbers())) {
                            log.debug("记录已存在且有效，跳过: {}, period: {}", serverPrefix, period);
                            return false;
                        }
                        
                        // 如果记录存在但号码是"---"，则更新
                        if (existingRecord != null) {
                            log.info("发现无效记录，进行更新: {}, period: {}, 旧号码: {}, 新号码: {}", 
                                   serverPrefix, period, existingRecord.getNumbers(), numbers);
                            
                            existingRecord.setNumbers(numbers);
                            existingRecord.setPrediction(prediction);
                            existingRecord.setOutcome(outcome);
                            try {
                                Integer totalNumberInt = Integer.parseInt(totalNumber);
                                existingRecord.setTotalNumber(totalNumberInt);
                            } catch (NumberFormatException e) {
                                existingRecord.setTotalNumber(null);
                            }
                            existingRecord.setKillNumber(killNumber);
                            
                            // 分析下注结果
                            BettingResult sf5ExistingBettingResult = BettingResultAnalyzer.analyzeFromHistoryRecord(
                                prediction, outcome, killNumber);
                            existingRecord.setBettingResult(sf5ExistingBettingResult.getDescription());
                            
                            // 设置开奖结果（单或双）
                            String sf5ExistingOpenResult = NumberAnalysisUtil.determineOddEven(totalNumber);
                            existingRecord.setOpenResult(sf5ExistingOpenResult);
                            
                            sf5Repository.save(existingRecord);
                            return true;
                        }
                    }
                    
                    // 创建新记录
                    Sf5HistoryRecord sf5Record = new Sf5HistoryRecord();
                    try {
                        Integer periodInt5Record = Integer.parseInt(period);
                        sf5Record.setPeriod(periodInt5Record);
                    } catch (NumberFormatException e) {
                        sf5Record.setPeriod(null);
                    }
                    sf5Record.setNumbers(numbers);
                    sf5Record.setPrediction(prediction);
                    sf5Record.setOutcome(outcome);
                    try {
                        Integer totalNumberInt = Integer.parseInt(totalNumber);
                        sf5Record.setTotalNumber(totalNumberInt);
                    } catch (NumberFormatException e) {
                        sf5Record.setTotalNumber(null);
                    }
                    sf5Record.setKillNumber(killNumber);
                    
                    // 分析下注结果
                    BettingResult sf5BettingResult = BettingResultAnalyzer.analyzeFromHistoryRecord(
                        prediction, outcome, killNumber);
                    sf5Record.setBettingResult(sf5BettingResult.getDescription());
                    
                    // 设置开奖结果（单或双）
                    String sf5OpenResult = NumberAnalysisUtil.determineOddEven(totalNumber);
                    sf5Record.setOpenResult(sf5OpenResult);
                    
                    sf5Repository.save(sf5Record);
                    return true;
                    
                case "sf6":
                    // 将period转换为Integer类型
                    Integer periodInt6;
                    try {
                        periodInt6 = Integer.parseInt(period);
                    } catch (NumberFormatException e) {
                        log.error("期号转换为整数失败: {}", period);
                        return false;
                    }
                    
                    // 检查记录是否存在
                    if (sf6Repository.existsByPeriod(periodInt6)) {
                        // 找到存在的记录
                        Sf6HistoryRecord existingRecord = sf6Repository.findByPeriod(periodInt6).orElse(null);
                        
                        // 如果记录存在且号码不是"---"，则跳过
                        if (existingRecord != null && !"---".equals(existingRecord.getNumbers()) && !"-".equals(existingRecord.getNumbers())) {
                            log.debug("记录已存在且有效，跳过: {}, period: {}", serverPrefix, period);
                            return false;
                        }
                        
                        // 如果记录存在但号码是"---"，则更新
                        if (existingRecord != null) {
                            log.info("发现无效记录，进行更新: {}, period: {}, 旧号码: {}, 新号码: {}", 
                                   serverPrefix, period, existingRecord.getNumbers(), numbers);
                            
                            existingRecord.setNumbers(numbers);
                            existingRecord.setPrediction(prediction);
                            existingRecord.setOutcome(outcome);
                            try {
                                Integer totalNumberInt = Integer.parseInt(totalNumber);
                                existingRecord.setTotalNumber(totalNumberInt);
                            } catch (NumberFormatException e) {
                                existingRecord.setTotalNumber(null);
                            }
                            existingRecord.setKillNumber(killNumber);
                            
                            // 分析下注结果
                            BettingResult sf6ExistingBettingResult = BettingResultAnalyzer.analyzeFromHistoryRecord(
                                prediction, outcome, killNumber);
                            existingRecord.setBettingResult(sf6ExistingBettingResult.getDescription());
                            
                            // 设置开奖结果（单或双）
                            String sf6ExistingOpenResult = NumberAnalysisUtil.determineOddEven(totalNumber);
                            existingRecord.setOpenResult(sf6ExistingOpenResult);
                            
                            sf6Repository.save(existingRecord);
                            return true;
                        }
                    }
                    
                    // 创建新记录
                    Sf6HistoryRecord sf6Record = new Sf6HistoryRecord();
                    try {
                        Integer periodInt6Record = Integer.parseInt(period);
                        sf6Record.setPeriod(periodInt6Record);
                    } catch (NumberFormatException e) {
                        sf6Record.setPeriod(null);
                    }
                    sf6Record.setNumbers(numbers);
                    sf6Record.setPrediction(prediction);
                    sf6Record.setOutcome(outcome);
                    try {
                        Integer totalNumberInt = Integer.parseInt(totalNumber);
                        sf6Record.setTotalNumber(totalNumberInt);
                    } catch (NumberFormatException e) {
                        sf6Record.setTotalNumber(null);
                    }
                    sf6Record.setKillNumber(killNumber);
                    
                    // 分析下注结果
                    BettingResult sf6BettingResult = BettingResultAnalyzer.analyzeFromHistoryRecord(
                        prediction, outcome, killNumber);
                    sf6Record.setBettingResult(sf6BettingResult.getDescription());
                    
                    // 设置开奖结果（单或双）
                    String sf6OpenResult = NumberAnalysisUtil.determineOddEven(totalNumber);
                    sf6Record.setOpenResult(sf6OpenResult);
                    
                    sf6Repository.save(sf6Record);
                    return true;
                    
                case "sf7":
                    // 将period转换为Integer类型
                    Integer periodInt7;
                    try {
                        periodInt7 = Integer.parseInt(period);
                    } catch (NumberFormatException e) {
                        log.error("期号转换为整数失败: {}", period);
                        return false;
                    }
                    
                    // 检查记录是否存在
                    if (sf7Repository.existsByPeriod(periodInt7)) {
                        // 找到存在的记录
                        Sf7HistoryRecord existingRecord = sf7Repository.findByPeriod(periodInt7).orElse(null);
                        
                        // 如果记录存在且号码不是"---"，则跳过
                        if (existingRecord != null && !"---".equals(existingRecord.getNumbers()) && !"-".equals(existingRecord.getNumbers())) {
                            log.debug("记录已存在且有效，跳过: {}, period: {}", serverPrefix, period);
                            return false;
                        }
                        
                        // 如果记录存在但号码是"---"，则更新
                        if (existingRecord != null) {
                            log.info("发现无效记录，进行更新: {}, period: {}, 旧号码: {}, 新号码: {}", 
                                   serverPrefix, period, existingRecord.getNumbers(), numbers);
                            
                            existingRecord.setNumbers(numbers);
                            existingRecord.setPrediction(prediction);
                            existingRecord.setOutcome(outcome);
                            try {
                                Integer totalNumberInt = Integer.parseInt(totalNumber);
                                existingRecord.setTotalNumber(totalNumberInt);
                            } catch (NumberFormatException e) {
                                existingRecord.setTotalNumber(null);
                            }
                            existingRecord.setKillNumber(killNumber);
                            
                            // 分析下注结果
                            BettingResult sf7ExistingBettingResult = BettingResultAnalyzer.analyzeFromHistoryRecord(
                                prediction, outcome, killNumber);
                            existingRecord.setBettingResult(sf7ExistingBettingResult.getDescription());
                            
                            // 设置开奖结果（单或双）
                            String sf7ExistingOpenResult = NumberAnalysisUtil.determineOddEven(totalNumber);
                            existingRecord.setOpenResult(sf7ExistingOpenResult);
                            
                            sf7Repository.save(existingRecord);
                            return true;
                        }
                    }
                    
                    // 创建新记录
                    Sf7HistoryRecord sf7Record = new Sf7HistoryRecord();
                    try {
                        Integer periodInt7Record = Integer.parseInt(period);
                        sf7Record.setPeriod(periodInt7Record);
                    } catch (NumberFormatException e) {
                        sf7Record.setPeriod(null);
                    }
                    sf7Record.setNumbers(numbers);
                    sf7Record.setPrediction(prediction);
                    sf7Record.setOutcome(outcome);
                    try {
                        Integer totalNumberInt = Integer.parseInt(totalNumber);
                        sf7Record.setTotalNumber(totalNumberInt);
                    } catch (NumberFormatException e) {
                        sf7Record.setTotalNumber(null);
                    }
                    sf7Record.setKillNumber(killNumber);
                    
                    // 分析下注结果
                    BettingResult sf7BettingResult = BettingResultAnalyzer.analyzeFromHistoryRecord(
                        prediction, outcome, killNumber);
                    sf7Record.setBettingResult(sf7BettingResult.getDescription());
                    
                    // 设置开奖结果（单或双）
                    String sf7OpenResult = NumberAnalysisUtil.determineOddEven(totalNumber);
                    sf7Record.setOpenResult(sf7OpenResult);
                    
                    sf7Repository.save(sf7Record);
                    return true;
                    
                default:
                    log.warn("未知的服务器前缀: {}", serverPrefix);
                    return false;
            }
        } catch (Exception e) {
            log.error("保存历史记录时发生错误: {}, 服务器: {}, 期号: {}", 
                    e.getMessage(), serverPrefix, period, e);
            return false;
        }
    }
}
