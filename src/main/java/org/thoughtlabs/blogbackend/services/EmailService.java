package org.thoughtlabs.blogbackend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thoughtlabs.blogbackend.exceptions.EmailFailureException;
import org.thoughtlabs.blogbackend.exceptions.EmailNotFoundException;
import org.thoughtlabs.blogbackend.models.User;
import org.thoughtlabs.blogbackend.models.VerificationToken;

@Service
public class EmailService {

    @Value("${email.from}")
    private String fromEmailAddress;

    @Value("${frontend.url}")
    private String url;

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendEmailVerificationEmail(VerificationToken verificationToken) throws EmailFailureException, MessagingException {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String verificationUrl = url + "/verify-email?token=" + verificationToken.getToken();

            String emailVerificationContent = "<p>Dear " + verificationToken.getUser().getFirstName()  + ",</p>"
                    + "<p>Thank you for registering with <b>ThoughtLabs</b>! To complete your registration, please verify your email address by clicking the link below:</p>"
                    + "<p><a href=\"" + verificationUrl + "\" style=\"background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none;\">Verify Email</a></p>"
                    + "<p>If the button above doesn't work, copy and paste this link into your browser:</p>"
                    + "<p><a href=\"" + verificationUrl + "\">" + verificationUrl + "</a></p>"
                    + "<p>This verification link will expire in <b>24 hours</b>.</p>"
                    + "<p>If you did not create an account, you can safely ignore this email.</p>"
                    + "<p>Best regards,</p>"
                    + "<p><b>ThoughtLabs Co</b></p>";

            helper.setTo(verificationToken.getUser().getEmail());
            helper.setFrom(fromEmailAddress);
            helper.setSubject("Verify your Email for ThoughtLabs");
            helper.setText(emailVerificationContent, true);

            javaMailSender.send(message);
        } catch (MailException | MessagingException ex) {
            throw new EmailFailureException("Failed to send email to " + verificationToken.getUser().getEmail(), ex);
        }
    }

    public void sendPasswordResetEmail(User user, String token) throws EmailFailureException {
        try{
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String resetPasswordUrl = url + "/reset-password?token=" + token;
            String userFirstName = user.getFirstName().substring(0, 1).toUpperCase() + user.getFirstName().substring(1);

            String resetPasswordContent = "<p>Dear " + userFirstName + ",</p>"
                    + "<p>We received a request to reset your password for your <b>ThoughtLabs</b> account. If you made this request, please click the button below to reset your password:</p>"
                    + "<p><a href=\"" + resetPasswordUrl + "\" style=\"background-color: #f44336; color: white; padding: 10px 20px; text-decoration: none;\">Recover Your Account</a></p>"
                    + "<p>If the button above doesn't work, copy and paste this link into your browser:</p>"
                    + "<p><a href=\"" + resetPasswordUrl + "\">" + resetPasswordUrl + "</a></p>"
                    + "<p>This reset link will expire in <b>30 mins</b>.</p>"
                    + "<p>If you did not request a password reset, you can safely ignore this email.</p>"
                    + "<p>Best regards,</p>"
                    + "<p><b>ThoughtLabs Co</b></p>";

            helper.setTo(user.getEmail());
            helper.setFrom(fromEmailAddress);
            helper.setSubject("Password Change Notification");
            helper.setText(resetPasswordContent, true);

            javaMailSender.send(message);
        } catch(MailException | MessagingException ex) {
            throw new EmailFailureException("Failed to send email to " + user.getEmail(), ex);
        }
    }
}
