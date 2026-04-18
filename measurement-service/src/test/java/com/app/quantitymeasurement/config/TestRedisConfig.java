package com.app.quantitymeasurement.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.embedded.RedisServer;
import redis.embedded.RedisExecProvider;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Test configuration for embedded Redis server.
 * This configuration starts an embedded Redis instance for testing purposes.
 */
@TestConfiguration
@Profile("test")
public class TestRedisConfig {

    private static final Logger log = LoggerFactory.getLogger(TestRedisConfig.class);

    private RedisServer redisServer;

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    private static boolean isWindows() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().contains("win");
    }

    @PostConstruct
    public void startRedis() throws Exception {
        // NOTE:
        // `embedded-redis` is not reliably supported on Windows.
        // When running tests on Windows we skip starting an embedded Redis process
        // to keep the test suite cross-platform.
        if (isWindows()) {
            log.warn("Skipping embedded Redis startup on Windows. If your tests require Redis, run them on Linux/macOS or provide an external Redis instance.");
            return;
        }

        redisServer = RedisServer.builder()
                .redisExecProvider(RedisExecProvider.defaultProvider())
                .port(redisPort)
                .setting("maxmemory 128M")
                .build();
        redisServer.start();
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        org.springframework.data.redis.connection.RedisStandaloneConfiguration config = 
            new org.springframework.data.redis.connection.RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}