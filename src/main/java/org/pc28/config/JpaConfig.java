package org.pc28.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA配置类，启用JPA存储库自动扫描和事务管理
 */
@Configuration
@EnableJpaRepositories(basePackages = "org.pc28.repository")
@EnableTransactionManagement
public class JpaConfig {
    // 配置类不需要其他方法，注解已启用了JPA存储库的自动扫描
}
