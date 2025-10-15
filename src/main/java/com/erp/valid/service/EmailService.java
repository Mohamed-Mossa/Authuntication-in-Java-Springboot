package com.erp.valid.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            Thread.sleep(1000*100);

        }catch (Exception   e){
            e.printStackTrace();
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("email@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Email Verification - OTP Code");
        message.setText("Your OTP code is: " + otp + "\n\nThis code will expire in 5 minutes.\n\nIf you didn't request this, please ignore this email.");

        mailSender.send(message);
    }

    public void sendWelcomeEmail(String toEmail, String username) {




        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("your-email@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Welcome to AUTH System!");
        message.setText("Hello " + username + ",\n\nYour account has been successfully verified!\n\nWelcome aboard!");

        mailSender.send(message);
    }
}