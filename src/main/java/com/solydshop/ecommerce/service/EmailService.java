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

    public void sendPasswordResetEmail(String toEmail, String resetToken, String frontendUrl) {
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Password Reset Request");
        message.setText(
                "You requested a password reset.\n\n" +
                "Click the link below to reset your password (expires in 15 minutes):\n\n" +
                resetLink + "\n\n" +
                "If you did not request this, please ignore this email."
        );

        mailSender.send(message);
    }

    public void sendPasswordChangeConfirmationEmail(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Your Password Has Been Changed");
        message.setText(
                "Your password was successfully changed.\n\n" +
                "If you did not make this change, please contact support immediately."
        );
        mailSender.send(message);
    }
}
