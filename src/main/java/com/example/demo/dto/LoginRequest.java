package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank
    @Schema(example = "ivan.ivanov@gmail.com")
    private String login;

    @NotBlank
    @Size(min = 8, max = 500)
    @Schema(example = "Password1!")
    private String password;
}
