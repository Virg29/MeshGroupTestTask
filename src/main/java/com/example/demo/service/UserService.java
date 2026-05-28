package com.example.demo.service;

import com.example.demo.dto.UserResponse;
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

import java.util.stream.Collectors;

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
        User user = fetchUser(userId);
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
        User user = fetchUser(userId);
        PhoneData phoneData = new PhoneData();
        phoneData.setUser(user);
        phoneData.setPhone(phone);
        phoneDataRepository.save(phoneData);
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setDateOfBirth(user.getDateOfBirth());
        response.setBalance(user.getAccount() != null ? user.getAccount().getBalance() : null);
        response.setEmails(user.getEmails().stream().map(EmailData::getEmail).collect(Collectors.toList()));
        response.setPhones(user.getPhones().stream().map(PhoneData::getPhone).collect(Collectors.toList()));

        return response;
    }

    @Transactional
    public void deleteEmail(Long userId, Long emailId) {
        EmailData emailData = emailDataRepository.findById(emailId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email not found"));
        if (!emailData.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        emailDataRepository.delete(emailData);
    }

    @Transactional
    public void deletePhone(Long userId, Long phoneId) {
        PhoneData phoneData = phoneDataRepository.findById(phoneId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Phone not found"));
        if (!phoneData.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        phoneDataRepository.delete(phoneData);
    }

    private User fetchUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
