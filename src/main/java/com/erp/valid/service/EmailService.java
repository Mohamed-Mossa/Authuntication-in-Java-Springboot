package com.erp.valid.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
@Slf4j

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

        @Async("taskExecutor")
        public void sendOtpEmail(String toEmail, String otp) {
            try {
                Thread.sleep(1000);
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("email@gmail.com");
                message.setTo(toEmail);
                message.setSubject("Email Verification - OTP Code");
                message.setText("Your OTP code is: " + otp +
                        "\n\nThis code will expire in 5 minutes." +
                        "\n\nIf you didn't request this, please ignore this email.");

                mailSender.send(message);
                log.info("OTP email sent successfully to: {}", toEmail);
            } catch (Exception e) {
                log.error("Failed to send OTP email to: {}", toEmail, e);
                // Consider implementing a retry mechanism or dead letter queue
            }
        }


        // Set as daemon thread so it doesn't prevent JVM shutdown

    @Async("taskExecutor")
    public void sendWelcomeEmail(String toEmail, String username) {

        try {

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("your-email@gmail.com");
            message.setTo(toEmail);
            message.setSubject("Welcome to AUTH System!");
            message.setText("Hello " + username + ",\n\nYour account has been successfully verified!\n\nWelcome aboard!");
            mailSender.send(message);

        } catch (Exception e) {
            log.error("Failed to send Welcome email to: {}", toEmail, e);
        }

    }
}