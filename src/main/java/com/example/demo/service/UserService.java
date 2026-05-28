package com.example.demo.service;

import com.example.demo.dto.UserPageResponse;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.EmailData;
import com.example.demo.entity.PhoneData;
import com.example.demo.entity.User;
import com.example.demo.repository.EmailDataRepository;
import com.example.demo.repository.PhoneDataRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailDataRepository emailDataRepository;
    private final PhoneDataRepository phoneDataRepository;

    @CacheEvict(value = "users-search", allEntries = true)
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

    @CacheEvict(value = "users-search", allEntries = true)
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

        return toResponse(user);
    }

    @CacheEvict(value = "users-search", allEntries = true)
    @Transactional
    public void deleteEmail(Long userId, Long emailId) {
        EmailData emailData = emailDataRepository.findById(emailId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email not found"));
        if (!emailData.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        emailDataRepository.delete(emailData);
    }

    @CacheEvict(value = "users-search", allEntries = true)
    @Transactional
    public void deletePhone(Long userId, Long phoneId) {
        PhoneData phoneData = phoneDataRepository.findById(phoneId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Phone not found"));
        if (!phoneData.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        phoneDataRepository.delete(phoneData);
    }

    @Cacheable(value = "users-search", key = "{#name, #dateOfBirth, #email, #phone, #pageable.pageNumber, #pageable.pageSize}")
    @Transactional(readOnly = true)
    public UserPageResponse searchUsers(String name, LocalDate dateOfBirth, String email, String phone, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> cb.conjunction();
        if (name != null)        spec = spec.and(UserSpecification.hasNameLike(name));
        if (dateOfBirth != null) spec = spec.and(UserSpecification.hasDateOfBirthAfter(dateOfBirth));
        if (email != null)       spec = spec.and(UserSpecification.hasEmail(email));
        if (phone != null)       spec = spec.and(UserSpecification.hasPhone(phone));

        return UserPageResponse.of(userRepository.findAll(spec, pageable).map(this::toResponse));
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setDateOfBirth(user.getDateOfBirth());
        response.setBalance(user.getAccount() != null ? user.getAccount().getBalance() : null);
        response.setEmails(user.getEmails().stream().map(EmailData::getEmail).collect(Collectors.toList()));
        response.setPhones(user.getPhones().stream().map(PhoneData::getPhone).collect(Collectors.toList()));
        return response;
    }

    private User fetchUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
