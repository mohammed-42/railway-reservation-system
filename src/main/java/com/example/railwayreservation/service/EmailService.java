package com.example.railwayreservation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendTicket(String to, String subject, String body) {
        // Do NOT crash app if mail is unavailable
        if (mailSender == null) {
            System.out.println("⚠️ MailSender not configured. Skipping email.");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("✅ Email sent to " + to);
        } catch (Exception e) {
            // Never let email failure break booking
            System.out.println("⚠️ Email failed: " + e.getMessage());
        }
    }
}
