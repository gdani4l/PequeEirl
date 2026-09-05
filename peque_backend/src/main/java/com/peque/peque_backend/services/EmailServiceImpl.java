package com.peque.peque_backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public void sendVerificationEmail(String to, String token) {
        String subject = "Te haz registrado correctamente";
        String confirmationUrl = baseUrl + "/api/auth/verify?token=" + token;
        String htmlContent = "<html><body>" +
                "<h2>¡Bienvenido a Peque Eirl!</h2>" +
                "<p>Haz clic en el siguiente enlace para activar tu cuenta:</p>" +
                "<a href=\"" + confirmationUrl + "\">Activar mi cuenta</a>" +
                "</body></html>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo de verificación", e);
        }
    }

    @Override
    public void sendMfaCode(String to, String codigo) {
        String subject = "Tu codigo de verificacion - Peque POS";
        String htmlContent = "<html><body>" +
                "<h2>Codigo de verificacion</h2>" +
                "<p>Usa el siguiente codigo para completar tu inicio de sesion:</p>" +
                "<h1 style=\"letter-spacing: 6px; font-family: monospace;\">" + codigo + "</h1>" +
                "<p>Este codigo expira en 5 minutos. Si no intentaste iniciar sesion, ignora este correo.</p>" +
                "</body></html>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el codigo de verificacion", e);
        }
    }

    @Override
    public void sendEmailWithAttachment(String to, String subject, String text, byte[] attachment, String attachmentName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);
            helper.setFrom(fromEmail);
            helper.addAttachment(attachmentName, new org.springframework.core.io.ByteArrayResource(attachment));
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo con comprobante adjunto", e);
        }
    }
}