package com.peque.peque_backend.services;

public interface EmailService {
    void sendVerificationEmail(String to, String token);
    void sendMfaCode(String to, String codigo);
    void sendEmailWithAttachment(String to, String subject, String text, byte[] attachment, String attachmentName);
}