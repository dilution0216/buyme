package com.example.buyme.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String userEmail;
    private String userPassword;
}