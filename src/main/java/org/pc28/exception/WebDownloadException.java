package org.pc28.exception;

/**
 * 网页下载异常类
 */
public class WebDownloadException extends RuntimeException {
    
    /**
     * 默认构造函数
     */
    public WebDownloadException() {
        super();
    }
    
    /**
     * 带错误信息的构造函数
     * 
     * @param message 错误信息
     */
    public WebDownloadException(String message) {
        super(message);
    }
    
    /**
     * 带错误信息和原因的构造函数
     * 
     * @param message 错误信息
     * @param cause 原因
     */
    public WebDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 带原因的构造函数
     * 
     * @param cause 原因
     */
    public WebDownloadException(Throwable cause) {
        super(cause);
    }
} 