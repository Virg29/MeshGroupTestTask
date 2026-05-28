package com.example.demo.service;

import com.example.demo.repository.EmailDataRepository;
import com.example.demo.repository.PhoneDataRepository;
import com.example.demo.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmailDataRepository emailDataRepository;
    private final PhoneDataRepository phoneDataRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public String login(String login, String password) {
        var user = login.contains("@")
                ? emailDataRepository.findByEmail(login)
                        .orElseThrow(() -> new BadCredentialsException("Invalid credentials"))
                        .getUser()
                : phoneDataRepository.findByPhone(login)
                        .orElseThrow(() -> new BadCredentialsException("Invalid credentials"))
                        .getUser();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return tokenProvider.generateToken(user.getId());
    }
}
