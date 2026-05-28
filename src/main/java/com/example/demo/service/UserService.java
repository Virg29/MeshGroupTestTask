package com.example.demo.service;

import com.example.demo.entity.EmailData;
import com.example.demo.entity.PhoneData;
import com.example.demo.entity.User;
import com.example.demo.repository.EmailDataRepository;
import com.example.demo.repository.PhoneDataRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailDataRepository emailDataRepository;
    private final PhoneDataRepository phoneDataRepository;

    @Transactional
    public void addEmail(Long userId, String email) {
        if (emailDataRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }
        User user = getUser(userId);
        EmailData emailData = new EmailData();
        emailData.setUser(user);
        emailData.setEmail(email);
        emailDataRepository.save(emailData);
    }

    @Transactional
    public void addPhone(Long userId, String phone) {
        if (phoneDataRepository.existsByPhone(phone)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone already in use");
        }
        User user = getUser(userId);
        PhoneData phoneData = new PhoneData();
        phoneData.setUser(user);
        phoneData.setPhone(phone);
        phoneDataRepository.save(phoneData);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
