package com.app.quantitymeasurement.config;

import io.lettuce.core.RedisURI;
import io.lettuce.core.ClientOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.url}")
    private String redisUrl;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisURI redisURI = RedisURI.create(redisUrl);

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .useSsl()
                .disablePeerVerification()
                .build();

        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration(
                redisURI.getHost(), redisURI.getPort());
        serverConfig.setPassword(RedisPassword.of(new String(redisURI.getPassword())));
        serverConfig.setUsername(
                redisURI.getUsername() != null ? redisURI.getUsername() : "default");

        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }

    /**
     * Configures a StringRedisTemplate with String serializers for both keys and values.
     * Keys are stored as: {@code blacklist:jti:<jti-value>}
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}