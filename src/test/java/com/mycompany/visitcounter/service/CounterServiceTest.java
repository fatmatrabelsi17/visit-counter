package com.mycompany.visitcounter.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Test unitaire pur : Redis est simulé avec Mockito, aucun serveur nécessaire
class CounterServiceTest {

    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> ops = mock(ValueOperations.class);
    private final StringRedisTemplate template = mock(StringRedisTemplate.class);

    @Test
    void incrementsTheHitsKey() {
        when(template.opsForValue()).thenReturn(ops);
        when(ops.increment("hits")).thenReturn(5L);

        assertThat(new CounterService(template).incrementHits()).isEqualTo(5L);
        verify(ops).increment("hits");
    }

    @Test
    void returnsZeroWhenRedisReturnsNull() {
        when(template.opsForValue()).thenReturn(ops);
        when(ops.increment("hits")).thenReturn(null);

        assertThat(new CounterService(template).incrementHits()).isZero();
    }
}
