package org.pc28.constants;

/**
 * 网页下载相关常量类
 */
public class WebConstants {
    
    /**
     * 目标网页URL列表
     */
    public static final String[] TARGET_URLS = {
        "http://www.redtaotao.com/jnd28_sf1_msds.html"
    };
    
    /**
     * 原始目标网页URL (保留以兼容现有代码)
     */
    public static final String TARGET_URL = "http://www.redtaotao.com/jnd28_sf1_mszh.html";
    
    /**
     * 下载文件保存路径
     */
    public static final String SAVE_PATH = "download";
    
    /**
     * 下载文件基础名称
     */
    public static final String FILE_NAME_BASE = "redtaotao";
    
    /**
     * 文件扩展名
     */
    public static final String FILE_EXTENSION = ".html";
    
    /**
     * 获取带时间戳的文件名
     * @param urlIndex URL的索引（用于区分不同URL的下载文件）
     * @return 带时间戳的文件名，格式为：redtaotao_sf{server}_yyyyMMdd_HHmmss.html
     */
    public static String getTimestampedFileName(int urlIndex) {
        // 从URL中提取服务器编号
        String url = TARGET_URLS[urlIndex];
        // 提取形如sf1, sf6等服务器编号
        String serverName = url.contains("_sf") ? "sf" + url.split("_sf")[1].split("_")[0] : "sf" + (urlIndex + 1);
        
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = java.time.LocalDateTime.now().format(formatter);
        return FILE_NAME_BASE + "_" + serverName + "_" + timestamp + FILE_EXTENSION;
    }
    
    /**
     * 获取最新的HTML文件
     * @param directory 目录路径
     * @param serverPrefix 服务器前缀（例如：sf1, sf2等）
     * @return 最新的HTML文件路径，如果没有找到则返回null
     */
    public static String getLatestHtmlFile(String directory, String serverPrefix) {
        java.io.File dir = new java.io.File(directory);
        if (!dir.exists() || !dir.isDirectory()) {
            return null;
        }
        
        java.io.File[] files = dir.listFiles((d, name) -> 
            name.startsWith(FILE_NAME_BASE + "_" + serverPrefix + "_") && name.endsWith(FILE_EXTENSION));
            
        if (files == null || files.length == 0) {
            return null;
        }
        
        java.util.Arrays.sort(files, java.util.Comparator.comparing(java.io.File::lastModified).reversed());
        return files[0].getAbsolutePath();
    }
    
    /**
     * 请求超时时间（毫秒）
     * 增加到30秒，处理网络延迟问题
     */
    public static final int CONNECTION_TIMEOUT = 5000;
    
    /**
     * 请求读取超时时间（毫秒）
     * 增加到30秒，处理网络延迟问题
     */
    public static final int READ_TIMEOUT = 5000;
    
    /**
     * 最大重试次数
     */
    public static final int MAX_RETRY_COUNT = 3;
    
    /**
     * 重试等待时间（毫秒）
     */
    public static final int RETRY_WAIT_TIME = 2000;
    
    /**
     * 用户代理头信息
     */
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    
    /**
     * 定时任务缓冲时间（秒）
     * 在倒计时结束后额外等待的时间，确保新数据已生成
     */
    public static final int TASK_BUFFER_SECONDS = 2;
    
    /**
     * 定时任务默认间隔（秒）
     * 当无法获取倒计时时使用的默认间隔
     */
    public static final int TASK_DEFAULT_INTERVAL = 3;
    
    /**
     * 是否显示倒计时进度条
     * 设置为false可以关闭倒计时显示，避免与Shell交互冲突
     */
    public static final boolean SHOW_COUNTDOWN_PROGRESS = true;
}