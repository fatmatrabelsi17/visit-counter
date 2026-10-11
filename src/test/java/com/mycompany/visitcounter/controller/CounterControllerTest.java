package com.mycompany.visitcounter.controller;

import com.mycompany.visitcounter.service.CounterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Test de la couche web uniquement : le service (donc Redis) est remplacé par un mock
@WebMvcTest(CounterController.class)
class CounterControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CounterService counterService;

    @Test
    void displaysCounterAndContainerId() throws Exception {
        when(counterService.incrementHits()).thenReturn(3L);

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(startsWith("Bonjour ! Cette page a été vue 3 fois.")))
                .andExpect(content().string(containsString("Je suis le conteneur ")));
    }

    @Test
    void returns503WhenRedisIsDown() throws Exception {
        when(counterService.incrementHits()).thenThrow(new RedisConnectionFailureException("down"));

        mvc.perform(get("/")).andExpect(status().isServiceUnavailable());
    }

    @Test
    void healthEndpoint() throws Exception {
        mvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }
}
