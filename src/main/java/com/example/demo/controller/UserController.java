package com.example.demo.controller;

import com.example.demo.dto.AddEmailRequest;
import com.example.demo.dto.AddPhoneRequest;
import com.example.demo.dto.TransferRequest;
import com.example.demo.dto.UserPageResponse;
import com.example.demo.dto.UserResponse;
import com.example.demo.service.TransferService;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TransferService transferService;

    @GetMapping("/search")
    public UserPageResponse search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate dateOfBirth,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return userService.searchUsers(name, dateOfBirth, email, phone, PageRequest.of(page, size, Sort.by("id")));
    }

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

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void transfer(@RequestBody @Valid TransferRequest request,
                         Authentication authentication) {
        transferService.transfer((Long) authentication.getPrincipal(), request.getRecipientUserId(), request.getAmount());
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
