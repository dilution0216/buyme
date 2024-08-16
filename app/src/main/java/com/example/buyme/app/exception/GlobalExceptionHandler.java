package com.example.buyme.app.exception;

import com.example.buyme.exception.DuplicateEmailException;
import com.example.buyme.exception.EmailNotVerifiedException;
import com.example.buyme.exception.InvalidEmailFormatException;
import com.example.buyme.exception.InvalidPhoneNumberException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<String> handleDuplicateEmailException(DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidEmailFormatException.class)
    public ResponseEntity<String> handleInvalidEmailFormatException(InvalidEmailFormatException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidPhoneNumberException.class)
    public ResponseEntity<String> handleInvalidPhoneNumberException(InvalidPhoneNumberException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<String> handleEmailNotVerifiedException(EmailNotVerifiedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

}