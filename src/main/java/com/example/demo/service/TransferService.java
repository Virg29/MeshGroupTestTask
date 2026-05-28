package com.example.demo.service;

import com.example.demo.entity.Account;
import com.example.demo.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;

    @CacheEvict(value = "users-search", allEntries = true)
    @Transactional
    public void transfer(Long donorUserId, Long recipientUserId, BigDecimal amount) {
        if (donorUserId.equals(recipientUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot transfer to yourself");
        }

        Account donorAccount = accountRepository.findByUserId(donorUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Donor account not found"));
        Account recipientAccount = accountRepository.findByUserId(recipientUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipient account not found"));

        // блокируем в порядке возрастания ID — защита от дедлока
        Long firstId  = Math.min(donorAccount.getId(), recipientAccount.getId());
        Long secondId = Math.max(donorAccount.getId(), recipientAccount.getId());

        Account first  = accountRepository.findByIdWithLock(firstId).orElseThrow();
        Account second = accountRepository.findByIdWithLock(secondId).orElseThrow();

        Account donor     = first.getId().equals(donorAccount.getId()) ? first : second;
        Account recipient = first.getId().equals(recipientAccount.getId()) ? first : second;

        if (donor.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance");
        }

        donor.setBalance(donor.getBalance().subtract(amount));
        recipient.setBalance(recipient.getBalance().add(amount));
    }
}
