package com.example.buyme.user.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String userEmail;
    private String userPassword;
}