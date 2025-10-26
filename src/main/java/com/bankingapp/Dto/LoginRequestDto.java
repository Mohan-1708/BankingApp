package com.bankingapp.Dto;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String email;
    private String password;
}