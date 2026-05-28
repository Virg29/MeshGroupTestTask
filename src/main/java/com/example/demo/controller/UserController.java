package com.example.demo.controller;

import com.example.demo.dto.AddEmailRequest;
import com.example.demo.dto.AddPhoneRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse getMe(Authentication authentication) {
        return userService.getUser((Long) authentication.getPrincipal());
    }

    @PostMapping("/email")
    @ResponseStatus(HttpStatus.CREATED)
    public void addEmail(@RequestBody @Valid AddEmailRequest request,
                         Authentication authentication) {
        userService.addEmail((Long) authentication.getPrincipal(), request.getEmail());
    }

    @PostMapping("/phone")
    @ResponseStatus(HttpStatus.CREATED)
    public void addPhone(@RequestBody @Valid AddPhoneRequest request,
                         Authentication authentication) {
        userService.addPhone((Long) authentication.getPrincipal(), request.getPhone());
    }

    @DeleteMapping("/email/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEmail(@PathVariable Long id, Authentication authentication) {
        userService.deleteEmail((Long) authentication.getPrincipal(), id);
    }

    @DeleteMapping("/phone/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePhone(@PathVariable Long id, Authentication authentication) {
        userService.deletePhone((Long) authentication.getPrincipal(), id);
    }
}
