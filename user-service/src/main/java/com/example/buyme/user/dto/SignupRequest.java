package com.example.buyme.user.dto;

import lombok.Data;

@Data
public class SignupRequest {
    private String userName;
    private String userEmail;
    private String userPassword;
    private String userPhoneNumber;
    private String address;
}