package com.mycompany.visitcounter.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CounterService {

    static final String HITS_KEY = "hits";

    private final StringRedisTemplate redisTemplate;

    public CounterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // INCR est atomique côté Redis : plusieurs conteneurs web peuvent incrémenter en même temps
    public long incrementHits() {
        Long hits = redisTemplate.opsForValue().increment(HITS_KEY);
        return hits == null ? 0 : hits;
    }
}
