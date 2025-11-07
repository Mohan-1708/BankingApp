package com.bankingapp.Service;

import com.bankingapp.Model.User;
import java.math.BigDecimal;

public interface EmailService {

    /**
     * Sends a transaction notification email asynchronously.
     *
     * @param user The user to send the email to.
     * @param subject The subject line of the email.
     * @param amount The amount of the transaction.
     * @param description A brief message about the transaction.
     * @param isCredit True if it's a credit (deposit), false if it's a debit (transfer out).
     */
    void sendTransactionNotification(User user, String subject, BigDecimal amount, String description, boolean isCredit);

    /**
     * Sends a simple OTP email.
     *
     * @param toEmail The recipient's email.
     * @param subject The subject line.
     * @param otp The 6-digit OTP string.
     */
    void sendOtpEmail(String toEmail,String name, String subject, String otp);
}