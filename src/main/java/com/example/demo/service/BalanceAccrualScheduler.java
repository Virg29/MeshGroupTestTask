package com.example.demo.service;

import com.example.demo.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BalanceAccrualScheduler {

    private final AccountRepository accountRepository;
    private final BalanceAccrualService balanceAccrualService;

    @Scheduled(fixedDelay = 30_000)
    public void accrueBalances() {
        var ids = accountRepository.findIdsWithBalanceBelowTarget();
        if (ids.isEmpty()) return;

        log.debug("Accruing balance for {} accounts", ids.size());
        ids.forEach(balanceAccrualService::accrueBalance);
    }
}
