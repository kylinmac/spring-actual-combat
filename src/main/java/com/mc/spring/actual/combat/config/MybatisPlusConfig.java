package com.mc.spring.actual.combat.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.mc.spring.actual.combat.injector.CustomizedSqlInjector;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@MapperScan(basePackages = "com.mc.spring.actual.combat.mapper")
@EnableTransactionManagement(proxyTargetClass = true)
public class MybatisPlusConfig {

    @Bean
    public CustomizedSqlInjector mySqlInjector() {
        return new CustomizedSqlInjector();
    }

    @Bean
    public MybatisPlusInterceptor paginationInterceptor() {
        MybatisPlusInterceptor page = new MybatisPlusInterceptor();
        return page;
    }

}
