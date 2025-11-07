package com.bankingapp.Dto;

import lombok.Data;

@Data
public class OtpVerificationDto {
    private String email;
    private String otp;
    private String name;
    private String password;

    // This field helps us know if we are verifying a login or a registration
    private String type; // "login" or "register"
}