package org.pc28.shell;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 应用程序启动后显示命令提示信息
 */
@Component
public class StartupMessageProvider implements ApplicationListener<ApplicationStartedEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        // 应用启动后输出一次命令提示信息
        System.out.println();
        System.out.println("=========== 欢迎使用PC28网页下载工具 ===========");
        System.out.println("输入命令 1 或 download - 下载默认网页内容");
        System.out.println("输入命令 2 或 download-index -i 索引 - 下载指定索引的网页");
        System.out.println("输入命令 3 或 download-all - 下载所有配置的网页");
        System.out.println("输入命令 7 或 parse-all - 解析所有服务器的最新数据");
        System.out.println("输入命令 8 或 save-to-db - 将已解析的数据保存到数据库");
        System.out.println("输入命令 help - 查看所有可用的命令");
        System.out.println("输入命令 exit 或 quit - 退出应用程序");
        System.out.println("=============================================");
        System.out.println();
    }
} 