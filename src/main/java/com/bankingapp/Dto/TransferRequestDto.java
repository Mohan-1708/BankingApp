package com.bankingapp.Dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequestDto {
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private String description;
}