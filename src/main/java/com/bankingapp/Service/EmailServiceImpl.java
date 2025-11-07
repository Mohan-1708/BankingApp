package com.bankingapp.Service;

import com.bankingapp.Model.User;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    // This ensures you are using your verified sender email
    @Value("${spring.mail.from}")
    private String fromEmail;

    /**
     * This is your transaction email method (unchanged)
     */
    @Override
    @Async
    public void sendTransactionNotification(User user, String subject, BigDecimal amount, String description, boolean isCredit) {
        try {
            log.info("Attempting to send transaction email to {}...", user.getEmail());
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            String htmlContent = buildHtmlContent(user.getName(), subject, amount, description, isCredit);
//            String name = user.getName();
            helper.setTo(user.getEmail());
            helper.setFrom(fromEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML
            mailSender.send(mimeMessage);
            log.info("Successfully sent transaction email to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send transaction email to {}: {}", user.getEmail(), e.getMessage());
            // This throw is helpful for debugging
            throw new RuntimeException("Email sending failed! See console logs.", e);
        }
    }

    /**
     * This is the updated OTP email method with your new template.
     */
    @Override
    @Async
    public void sendOtpEmail(String toEmail, String name, String subject, String otp) {
        try {
            log.info("Attempting to send OTP email to {}...", toEmail);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            // Use "User" as a fallback if no name is provided (like during login)
            String recipientName = (name == null || name.isBlank()) ? "User" : name;

            // --- THIS IS YOUR NEW TEMPLATE ---
            String htmlContent = "<div style='font-family: Arial, sans-serif; line-height: 1.6;'>"
                    + "<p>Hi, " + recipientName + "</p>"
                    + "<p>Your One-Time Password (OTP) for Java Dev Bank is:</p>"
                    + "<h1 style='color: #004e92; letter-spacing: 2px;'>" + otp + "</h1>"
                    + "<p>This OTP is valid for 5 minutes. Please do not share it with anyone.</p>"
                    + "<hr>"
                    + "<p><em>Java Dev Bank Inc. &copy; 2025</em></p>"
                    + "</div>";
            // --- END OF NEW TEMPLATE ---

            helper.setTo(toEmail);
            helper.setFrom(fromEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(mimeMessage);
            log.info("Successfully sent OTP email to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            // This throw is helpful for debugging
            throw new RuntimeException("OTP Email sending failed! See console logs.", e);
        }
    }

    // This helper is for transactions only (unchanged)
    private String buildHtmlContent(String name, String subject, BigDecimal amount, String description, boolean isCredit) {
        String color = isCredit ? "#28a745" : "#dc3545"; // Green for credit, Red for debit
        String transactionType = isCredit ? "Deposited" : "Transferred";
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        String formattedAmount = currencyFormatter.format(amount);

        return "<div style='font-family: Arial, sans-serif; line-height: 1.6;'>"
                + "<h2>Hi " + name + ",</h2>"
                + "<p>This is a notification for a transaction on your Java Dev Bank account.</p>"
                + "<hr>"
                + "<h3 style='color: " + color + ";'>" + subject + "</h3>"
                + "<p><strong>Amount:</strong> <span style='color: " + color + "; font-weight: bold;'>" + formattedAmount + "</span></p>"
                + "<p><strong>Type:</strong> " + transactionType + "</p>"
                + "<p><strong>Description:</strong> " + description + "</p>"
                + "<hr>"
                + "<p>Thank you for banking with us!</p>"
                + "<p><em>Java Dev Bank Inc. &copy; 2025</em></p>"
                + "</div>";
    }
}