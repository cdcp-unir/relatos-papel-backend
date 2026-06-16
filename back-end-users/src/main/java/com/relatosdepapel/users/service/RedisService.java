package com.relatosdepapel.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public void saveToken(String opaqueToken, String jwt) {

        redisTemplate.opsForValue()
                .set(
                        opaqueToken,
                        jwt,
                        Duration.ofHours(1)
                );
    }

    public String getJwt(String opaqueToken) {

        return redisTemplate.opsForValue()
                .get(opaqueToken);
    }

    public void deleteToken(String opaqueToken) {
        redisTemplate.delete(opaqueToken);
    }
}