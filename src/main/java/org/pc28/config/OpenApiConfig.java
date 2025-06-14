package org.pc28.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI文档配置类
 */
@Configuration
public class OpenApiConfig {
    
    /**
     * 配置OpenAPI文档信息
     * 
     * @return OpenAPI配置
     */
    @Bean
    public OpenAPI customOpenAPI() {
        // 创建并配置OpenAPI对象，用于定义API的文档信息
        return new OpenAPI()
                .components(new Components()) // 初始化组件，可在此添加全局使用的组件，如安全方案、响应等
                .info(new Info()
                        .title("pc28网页下载API") // 设置API文档标题
                        .description("这是一个用于下载pc28网站(http://www.redtaotao.com/jnd28_sf1_mszh.html)内容的API") // 设置API文档描述
                        .version("1.0.0") // 设置API文档版本
                        .contact(new Contact()
                                .name("onlynew") // 设置联系人名称
                                .email("support@example.com") // 设置联系人邮箱
                                .url("https://www.example.com")) // 设置联系人网址
                        .license(new License()
                                .name("Apache 2.0") // 设置许可证名称
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")) // 设置许可证URL
                );
    }
}