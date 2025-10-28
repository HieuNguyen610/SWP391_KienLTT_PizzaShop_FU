package com.swp.pizzashop.service;

import com.swp.pizzashop.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

public interface EmailService {
    void sendPasswordResetEmail(User user, String resetLink);
}
    public String sendOtpEmail(String toEmail) {
        String otp = String.format("%06d", new Random().nextInt(999999));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("🍕 PizzaShop - Mã xác thực OTP của bạn");
        message.setText("""
                Xin chào quý khách,

                Cảm ơn bạn đã đăng ký tài khoản tại PizzaShop!
                Mã OTP của bạn là: %s

                Mã này có hiệu lực trong vòng 5 phút.
                Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.

                Trân trọng,
                Đội ngũ PizzaShop 🍕
                """.formatted(otp));

        mailSender.send(message);
        return otp;
    }
}
