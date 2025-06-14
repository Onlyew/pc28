package org.pc28.task;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pc28.constants.WebConstants;
import org.pc28.service.WebDownloadService;
import org.pc28.service.db.HistoryDataService;
import org.pc28.utils.SimpleHtmlParser;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据采集定时任务
 * 根据实际开奖倒计时动态调度下载任务
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DataCollectionTask {

    private final WebDownloadService webDownloadService;
    private final HistoryDataService historyDataService;
    private final TaskScheduler taskScheduler;
    
    // 用于防止任务重叠执行
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    
    // 任务执行次数计数
    private int executionCount = 0;
    
    // 当前调度的任务
    private ScheduledFuture<?> currentTask;
    
    // 倒计时显示定时器
    private Timer countdownTimer;
    
    // 剩余秒数
    private final AtomicInteger remainingSeconds = new AtomicInteger(0);
    
    /**
     * 应用启动后初始化定时任务
     */
    @PostConstruct
    public void init() {
        log.info("数据采集任务调度器启动，10秒后执行第一次采集");
        // 启动10秒后执行第一次任务
        scheduleNextTask(10);
    }
    
    /**
     * 执行数据采集任务
     */
    public void collectData() {
        // 停止倒计时显示
        stopCountdownDisplay();
        
        // 防止任务重叠执行
        if (!isRunning.compareAndSet(false, true)) {
            log.warn("上一次任务还未完成，跳过本次执行");
            return;
        }
        
        try {
            executionCount++;
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            log.info("===== 开始第{}次数据采集任务 [{}] =====", executionCount, now);
            
            // 1. 清理旧文件
            log.info("步骤1: 清理下载文件夹中的旧文件");
            int deletedCount = cleanDownloadFolder();
            log.info("成功删除 {} 个旧HTML文件", deletedCount);
            
            // 2. 下载最新数据
            log.info("步骤2: 下载所有目标网页");
            int downloadedCount = webDownloadService.downloadAllUrls();
            log.info("成功下载 {} 个网页", downloadedCount);
            
            // 等待一秒确保文件写入完成
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("等待被中断");
            }
            
            // 3. 解析数据
            log.info("步骤3: 解析最新下载的HTML文件");
            List<Map<String, Object>> allResults = SimpleHtmlParser.parseAllServers();
            log.info("成功解析 {} 个服务器的数据", allResults.size());
            
            // 获取倒计时信息
            int nextExecutionSeconds = calculateNextExecutionTime(allResults);
            
            // 4. 保存到数据库
            log.info("步骤4: 将解析结果保存到数据库");
            Map<String, Integer> savedCounts = historyDataService.saveAllServersData(allResults);
            
            // 统计总保存记录数
            int totalSaved = savedCounts.values().stream().mapToInt(Integer::intValue).sum();
            log.info("成功保存 {} 条记录到数据库", totalSaved);
            
            for (Map.Entry<String, Integer> entry : savedCounts.entrySet()) {
                log.info("服务器 {}: 保存 {} 条记录", entry.getKey(), entry.getValue());
            }
            
            // 5. 任务结束日志
            log.info("数据采集任务已完成，最新数据已保存");
            log.info("===== 第{}次数据采集任务完成 =====", executionCount);
            
            // 6. 根据倒计时调度下一次任务
            scheduleNextTask(nextExecutionSeconds);
            
        } catch (Exception e) {
            log.error("数据采集任务执行异常: {}", e.getMessage(), e);
            // 出错时，30秒后重试
            scheduleNextTask(30);
        } finally {
            // 释放运行状态锁
            isRunning.set(false);
        }
    }
    
    /**
     * 根据解析结果计算下一次执行时间
     * @param allResults 所有服务器的解析结果
     * @return 下一次执行的秒数
     */
    private int calculateNextExecutionTime(List<Map<String, Object>> allResults) {
        if (allResults.isEmpty()) {
            log.warn("没有解析结果，使用默认间隔{}秒", WebConstants.TASK_DEFAULT_INTERVAL);
            return WebConstants.TASK_DEFAULT_INTERVAL;
        }
        
        // 获取第一个服务器的倒计时信息
        Map<String, Object> firstResult = allResults.get(0);
        String countdownMinutes = (String) firstResult.get("countdown_minutes");
        String countdownSeconds = (String) firstResult.get("countdown_seconds");
        
        if (countdownMinutes != null && countdownSeconds != null) {
            try {
                int minutes = Integer.parseInt(countdownMinutes);
                int seconds = Integer.parseInt(countdownSeconds);
                int totalSeconds = minutes * 60 + seconds;
                
                // 使用配置的缓冲时间
                int nextExecutionSeconds = totalSeconds + WebConstants.TASK_BUFFER_SECONDS;
                
                log.info("解析到倒计时: {}分{}秒（总计{}秒），增加{}秒缓冲时间，下次执行将在{}秒后", 
                    minutes, seconds, totalSeconds, WebConstants.TASK_BUFFER_SECONDS, nextExecutionSeconds);
                
                return nextExecutionSeconds;
            } catch (NumberFormatException e) {
                log.error("解析倒计时失败: {} 分 {} 秒", countdownMinutes, countdownSeconds);
            }
        }
        
        log.warn("未能获取倒计时信息，使用默认间隔{}秒", WebConstants.TASK_DEFAULT_INTERVAL);
        return WebConstants.TASK_DEFAULT_INTERVAL;
    }
    
    /**
     * 调度下一次任务执行
     * @param delaySeconds 延迟秒数
     */
    private void scheduleNextTask(int delaySeconds) {
        // 取消之前的任务（如果存在）
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(false);
        }
        
        // 计算下次执行时间
        Instant nextExecutionTime = Instant.now().plus(Duration.ofSeconds(delaySeconds));
        
        // 调度新任务
        currentTask = taskScheduler.schedule(this::collectData, nextExecutionTime);
        
        String nextTime = LocalDateTime.now().plusSeconds(delaySeconds)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("下一次数据采集已安排在: {} ({}秒后)", nextTime, delaySeconds);
        
        // 启动倒计时显示
        startCountdownDisplay(delaySeconds);
    }
    
    /**
     * 启动倒计时显示
     * @param totalSeconds 总秒数
     */
    private void startCountdownDisplay(int totalSeconds) {
        // 检查是否启用倒计时显示
        if (!WebConstants.SHOW_COUNTDOWN_PROGRESS) {
            return;
        }
        
        // 停止之前的倒计时（如果存在）
        stopCountdownDisplay();
        
        remainingSeconds.set(totalSeconds);
        countdownTimer = new Timer("Countdown-Timer", true);
        
        countdownTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int remaining = remainingSeconds.decrementAndGet();
                if (remaining >= 0) {
                    // 计算分钟和秒
                    int minutes = remaining / 60;
                    int seconds = remaining % 60;
                    
                    // 使用\r实现同行更新
                    System.out.print("\r【倒计时】距离下次采集: " + 
                        String.format("%02d分%02d秒", minutes, seconds) + 
                        " [" + generateProgressBar(totalSeconds - remaining, totalSeconds) + "]");
                    System.out.flush();
                } else {
                    System.out.println("\r【倒计时】正在执行数据采集...                                                  ");
                    cancel();
                }
            }
        }, 0, 1000); // 每秒更新一次
    }
    
    /**
     * 停止倒计时显示
     */
    private void stopCountdownDisplay() {
        if (countdownTimer != null) {
            countdownTimer.cancel();
            countdownTimer = null;
            // 清除倒计时行
            System.out.print("\r                                                                           \r");
            System.out.flush();
        }
    }
    
    /**
     * 生成进度条
     * @param current 当前进度
     * @param total 总进度
     * @return 进度条字符串
     */
    private String generateProgressBar(int current, int total) {
        int barLength = 20;
        int filled = (int) ((double) current / total * barLength);
        StringBuilder bar = new StringBuilder();
        
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        
        int percentage = (int) ((double) current / total * 100);
        return bar.toString() + " " + percentage + "%";
    }
    
    /**
     * 清理下载文件夹，删除所有HTML文件
     * @return 删除的文件数量
     */
    private int cleanDownloadFolder() {
        File downloadDir = new File(WebConstants.SAVE_PATH);
        if (!downloadDir.exists() || !downloadDir.isDirectory()) {
            log.warn("下载文件夹 {} 不存在", WebConstants.SAVE_PATH);
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
}
