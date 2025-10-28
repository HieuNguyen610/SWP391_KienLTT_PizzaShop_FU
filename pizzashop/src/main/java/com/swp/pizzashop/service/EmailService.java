package com.swp.pizzashop.service;

import com.swp.pizzashop.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;


public interface EmailService {
    void sendPasswordResetEmail(User user, String resetLink);
    void sendOtpEmail(String toEmail, String otp);
}


