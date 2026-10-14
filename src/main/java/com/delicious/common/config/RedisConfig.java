package com.delicious.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

/**
 * Production-ready Redis configuration with non-blocking resilience:
 * - Low connect & command timeouts so an offline Redis never hangs startup or requests.
 * - Validation on startup logs a friendly WARNING instead of crashing the server.
 * - StringRedisTemplate bean for high-throughput string operations (rate-limiting, cache).
 */
@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration();
        serverConfig.setHostName(redisHost);
        serverConfig.setPort(redisPort);
        if (redisPassword != null && !redisPassword.isBlank()) {
            serverConfig.setPassword(redisPassword);
        }

        // Configure client with tight timeouts so unavailable Redis fails fast without blocking threads
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(1500))
                .shutdownTimeout(Duration.ofMillis(500))
                .build();

        LettuceConnectionFactory factory = new LettuceConnectionFactory(serverConfig, clientConfig);
        // Do not validate connection eagerly at construction time so Spring Boot doesn't fail context startup if Redis is down
        factory.setValidateConnection(false);
        return factory;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        template.afterPropertiesSet();

        // Perform a safe health-check ping on startup
        try {
            var conn = redisConnectionFactory.getConnection();
            String pingResult = conn.ping();
            conn.close();
            log.info(" Redis connection established successfully at {}:{} (PING: {})", redisHost, redisPort, pingResult);
        } catch (Exception ex) {
            log.warn(" Redis connection failed at {}:{}. The application will continue running with graceful fallback. Error: {}",
                    redisHost, redisPort, ex.getMessage());
        }

        return template;
    }
}
