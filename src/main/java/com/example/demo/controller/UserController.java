package com.example.demo.controller;

import com.example.demo.dto.AddEmailRequest;
import com.example.demo.dto.AddPhoneRequest;
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
}
