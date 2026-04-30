package com.example.kafka.notificationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class ShardDataSourceConfig {

    @Bean(name = "shard0DataSource")
    @Primary
    public DataSource shard0DataSource(
            @Value("${app.sharding.targets.shard0.url:${spring.datasource.url}}") String url,
            @Value("${app.sharding.targets.shard0.username:${spring.datasource.username}}") String username,
            @Value("${app.sharding.targets.shard0.password:${spring.datasource.password}}") String password,
            @Value("${app.sharding.targets.shard0.driver-class-name:${spring.datasource.driver-class-name}}") String driverClassName
    ) {
        return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(url)
                .username(username)
                .password(password)
                .build();
    }

    @Bean(name = "shard1DataSource")
    public DataSource shard1DataSource(
            @Value("${app.sharding.targets.shard1.url}") String url,
            @Value("${app.sharding.targets.shard1.username}") String username,
            @Value("${app.sharding.targets.shard1.password}") String password,
            @Value("${app.sharding.targets.shard1.driver-class-name:com.mysql.cj.jdbc.Driver}") String driverClassName
    ) {
        return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(url)
                .username(username)
                .password(password)
                .build();
    }

    @Bean(name = "shard0JdbcTemplate")
    public JdbcTemplate shard0JdbcTemplate(@Qualifier("shard0DataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "shard1JdbcTemplate")
    public JdbcTemplate shard1JdbcTemplate(@Qualifier("shard1DataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}