package com.addon.rateLimit.cache;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.Duration;


@Component
public class APIHitsCache {
    private static final String KEY_TEMPLATE = "{0}::{1}";

    private final String serviceName;
    private final RedisTemplate<String, Object> rateLimitRedisTemplate;

    public APIHitsCache(@Value("${application.service-name}") String serviceName,
                        @Qualifier("rateLimitRedisTemplate") RedisTemplate<String, Object> rateLimitRedisTemplate) {
        this.serviceName = serviceName;
        this.rateLimitRedisTemplate = rateLimitRedisTemplate;

    }

    public Object getApiHitCount(String key) {
        return rateLimitRedisTemplate.
                opsForValue().get(MessageFormat.format(KEY_TEMPLATE, serviceName, key));
    }

    public void incrementApiHitCount(String key) {

        rateLimitRedisTemplate.
                opsForValue().
                increment(MessageFormat.format(KEY_TEMPLATE, serviceName, key));
    }

    public void decrementApiHitCount(String key) {

        rateLimitRedisTemplate.
                opsForValue().
                decrement(MessageFormat.format(KEY_TEMPLATE, serviceName, key));
    }

    public void addApiHitCount(String key, long ttl) {
        rateLimitRedisTemplate.
                opsForValue().set(MessageFormat.format(KEY_TEMPLATE, serviceName, key), "1", Duration.ofSeconds(ttl));
    }
}
