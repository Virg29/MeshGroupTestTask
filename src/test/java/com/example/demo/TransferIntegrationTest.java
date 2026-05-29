package com.example.demo;

import com.example.demo.repository.AccountRepository;
import com.example.demo.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TransferIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @SuppressWarnings("resource")
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7").withExposedPorts(6379);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtTokenProvider tokenProvider;
    @Autowired AccountRepository accountRepository;

    private static final Long USER1_ID = 1L;
    private static final Long USER2_ID = 2L;
    private static final Long NONEXISTENT_USER_ID = 999L;

    @BeforeEach
    void resetBalances() {
        accountRepository.findAll().forEach(account -> {
            account.setBalance(new BigDecimal("1000.00"));
            accountRepository.save(account);
        });
    }

    private String token(Long userId) {
        return "Bearer " + tokenProvider.generateToken(userId);
    }

    private String transferBody(Long recipientId, String amount) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "recipientUserId", recipientId,
                "amount", amount
        ));
    }

    private BigDecimal balanceOf(Long userId) {
        return accountRepository.findByUserId(userId).orElseThrow().getBalance();
    }

    @Test
    void transfer_success() throws Exception {
        mockMvc.perform(post("/user/transfer")
                        .header("Authorization", token(USER1_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferBody(USER2_ID, "100.00")))
                .andExpect(status().isNoContent());

        assertThat(balanceOf(USER1_ID)).isEqualByComparingTo("900.00");
        assertThat(balanceOf(USER2_ID)).isEqualByComparingTo("1100.00");
    }

    @Test
    void transfer_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/user/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferBody(USER2_ID, "100.00")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void transfer_selfTransfer_returns400() throws Exception {
        mockMvc.perform(post("/user/transfer")
                        .header("Authorization", token(USER1_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferBody(USER1_ID, "100.00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot transfer to yourself"));
    }

    @Test
    void transfer_insufficientBalance_returns400() throws Exception {
        mockMvc.perform(post("/user/transfer")
                        .header("Authorization", token(USER1_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferBody(USER2_ID, "9999.00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient balance"));
    }

    @Test
    void transfer_zeroAmount_returns400() throws Exception {
        mockMvc.perform(post("/user/transfer")
                        .header("Authorization", token(USER1_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferBody(USER2_ID, "0.00")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_negativeAmount_returns400() throws Exception {
        mockMvc.perform(post("/user/transfer")
                        .header("Authorization", token(USER1_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferBody(USER2_ID, "-50.00")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_nonexistentRecipient_returns404() throws Exception {
        mockMvc.perform(post("/user/transfer")
                        .header("Authorization", token(USER1_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferBody(NONEXISTENT_USER_ID, "100.00")))
                .andExpect(status().isNotFound());
    }

    @Test
    void transfer_concurrent_totalBalancePreserved() throws Exception {
        int threads = 20;
        BigDecimal amount = new BigDecimal("150.00");
        BigDecimal totalBefore = balanceOf(USER1_ID).add(balanceOf(USER2_ID));

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startGun = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threads / 2; i++) {
            futures.add(executor.submit(() -> {
                try {
                    startGun.await();
                    mockMvc.perform(post("/user/transfer")
                            .header("Authorization", token(USER1_ID))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(transferBody(USER2_ID, amount.toPlainString())));
                } catch (Exception ignored) {}
            }));
            futures.add(executor.submit(() -> {
                try {
                    startGun.await();
                    mockMvc.perform(post("/user/transfer")
                            .header("Authorization", token(USER2_ID))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(transferBody(USER1_ID, amount.toPlainString())));
                } catch (Exception ignored) {}
            }));
        }

        startGun.countDown();
        for (Future<?> f : futures) f.get();
        executor.shutdown();

        BigDecimal totalAfter = balanceOf(USER1_ID).add(balanceOf(USER2_ID));
        assertThat(totalAfter).isEqualByComparingTo(totalBefore);
    }
}
