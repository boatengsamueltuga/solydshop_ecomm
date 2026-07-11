package com.solydshop.ecommerce.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String SUPPORT_SIGNATURE =
            "\n\nNeed help? Contact SolydShop Support:\n" +
            "Phone: +1 573 466 6199\n" +
            "Email: boatengsamuel237@mail.com\n\n" +
            "— The SolydShop Team";

    public void sendPasswordResetEmail(String toEmail, String resetToken, String frontendUrl) {
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Password Reset Request – SolydShop");
        message.setText(
                "Hello,\n\n" +
                "We received a request to reset the password for your SolydShop account.\n\n" +
                "Click the link below to choose a new password (this link expires in 15 minutes):\n\n" +
                resetLink + "\n\n" +
                "If you didn't request a password reset, you can safely ignore this email " +
                "— your password will remain unchanged." +
                SUPPORT_SIGNATURE
        );

        mailSender.send(message);
    }

    public void sendPasswordChangeConfirmationEmail(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Your SolydShop Password Has Been Changed");
        message.setText(
                "Hello,\n\n" +
                "This is a confirmation that the password for your SolydShop account was " +
                "successfully changed.\n\n" +
                "If you made this change, no further action is needed.\n\n" +
                "If you did not request this change, please contact us immediately so we " +
                "can help secure your account." +
                SUPPORT_SIGNATURE
        );
        mailSender.send(message);
    }
}
