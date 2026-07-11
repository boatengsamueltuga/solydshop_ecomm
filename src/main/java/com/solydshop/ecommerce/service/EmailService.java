package com.solydshop.ecommerce.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String LOGO_RESOURCE_PATH = "email/logo.png";
    private static final String LOGO_CID = "solydLogo";

    private static final String SUPPORT_SIGNATURE_HTML =
            "<p style=\"margin:24px 0 0;font-size:14px;color:#475569;\">" +
            "Need help? Contact SolydShop Support:<br>" +
            "Phone: <a href=\"tel:5734666199\" style=\"color:#D4A373;text-decoration:none;\">+1 573 466 6199</a><br>" +
            "Email: <a href=\"mailto:boatengsamuel237@mail.com\" style=\"color:#D4A373;text-decoration:none;\">boatengsamuel237@mail.com</a>" +
            "</p>" +
            "<p style=\"margin:16px 0 0;font-size:14px;color:#475569;\">— The SolydShop Team</p>";

    public void sendPasswordResetEmail(String toEmail, String resetToken, String frontendUrl) {
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;

        String body =
                "<p>Hello,</p>" +
                "<p>We received a request to reset the password for your SolydShop account.</p>" +
                "<p>Click the button below to choose a new password (this link expires in 15 minutes):</p>" +
                "<p style=\"text-align:center;margin:28px 0;\">" +
                "<a href=\"" + resetLink + "\" style=\"background:#D4A373;color:#0B132B;font-weight:700;" +
                "text-decoration:none;padding:12px 28px;border-radius:6px;display:inline-block;\">Reset Password</a>" +
                "</p>" +
                "<p>If you didn't request a password reset, you can safely ignore this email " +
                "— your password will remain unchanged.</p>" +
                SUPPORT_SIGNATURE_HTML;

        sendBrandedEmail(toEmail, "Password Reset Request – SolydShop", body);
    }

    public void sendPasswordChangeConfirmationEmail(String toEmail) {
        String body =
                "<p>Hello,</p>" +
                "<p>This is a confirmation that the password for your SolydShop account was " +
                "successfully changed.</p>" +
                "<p>If you made this change, no further action is needed.</p>" +
                "<p>If you did not request this change, please contact us immediately so we " +
                "can help secure your account.</p>" +
                SUPPORT_SIGNATURE_HTML;

        sendBrandedEmail(toEmail, "Your SolydShop Password Has Been Changed", body);
    }

    /**
     * Wraps body HTML in the SolydShop branded template (dark navy header with
     * the gear logo + wordmark, matching the site's navbar) and sends it as a
     * multipart HTML email with the logo embedded inline via CID, so it
     * renders without depending on any externally-hosted image.
     */
    private void sendBrandedEmail(String toEmail, String subject, String bodyHtml) {
        String html =
                "<!DOCTYPE html><html><body style=\"margin:0;padding:0;background:#f1f5f9;" +
                "font-family:Arial,Helvetica,sans-serif;\">" +
                "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" " +
                "style=\"background:#f1f5f9;padding:32px 0;\">" +
                "<tr><td align=\"center\">" +
                "<table role=\"presentation\" width=\"480\" cellpadding=\"0\" cellspacing=\"0\" " +
                "style=\"background:#ffffff;border-radius:8px;overflow:hidden;\">" +
                "<tr><td style=\"background:#0B132B;padding:20px 24px;\">" +
                "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\"><tr>" +
                "<td style=\"padding-right:10px;\">" +
                "<img src=\"cid:" + LOGO_CID + "\" width=\"32\" height=\"32\" alt=\"SolydShop\" style=\"display:block;\">" +
                "</td>" +
                "<td style=\"font-size:22px;font-weight:700;color:#D4A373;" +
                "font-family:Arial,Helvetica,sans-serif;\">SolydShop</td>" +
                "</tr></table>" +
                "</td></tr>" +
                "<tr><td style=\"padding:28px 24px;font-size:15px;line-height:1.6;color:#0f172a;\">" +
                bodyHtml +
                "</td></tr>" +
                "</table>" +
                "</td></tr>" +
                "</table>" +
                "</body></html>";

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.addInline(LOGO_CID, new ClassPathResource(LOGO_RESOURCE_PATH));
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
