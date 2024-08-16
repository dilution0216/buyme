package com.example.buyme.app.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^010-?\\d{4}-?\\d{4}$");

    public static boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        return PHONE_PATTERN.matcher(phoneNumber).matches();
    }
}