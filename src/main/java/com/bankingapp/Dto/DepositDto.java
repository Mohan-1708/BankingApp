package com.bankingapp.Dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DepositDto {
    private String toAccountNumber;
    private BigDecimal amount;
    private String description;
}