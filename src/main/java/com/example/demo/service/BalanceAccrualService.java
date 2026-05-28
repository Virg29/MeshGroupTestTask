package com.example.demo.service;

import com.example.demo.entity.Account;
import com.example.demo.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class BalanceAccrualService {

    private static final BigDecimal RATE = BigDecimal.valueOf(1.10);

    private final AccountRepository accountRepository;

    @Transactional
    public void accrueBalance(Long accountId) {
        Account account = accountRepository.findByIdWithLock(accountId).orElse(null);
        if (account == null) return;

        if (account.getBalance().compareTo(account.getTargetBalanceStockpiling()) >= 0) return;

        BigDecimal newBalance = account.getBalance()
                .multiply(RATE)
                .setScale(2, RoundingMode.HALF_UP);

        if (newBalance.compareTo(account.getTargetBalanceStockpiling()) > 0) {
            newBalance = account.getTargetBalanceStockpiling();
        }

        account.setBalance(newBalance);
    }
}
