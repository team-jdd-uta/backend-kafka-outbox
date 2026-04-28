package com.example.kafka.userservice.service;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("email already exists: " + email);
    }
}