package com.form.form_back.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void sendResetPasswordEmail(String toEmail, String token) {
        try {
            String resetLink = "http://localhost:4200/reset-password?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noordev2025@outlook.com");
            message.setTo(toEmail);
            message.setSubject("Réinitialisation de votre mot de passe");
            message.setText("Bonjour,\n\n"
                    + "Vous avez demandé à réinitialiser votre mot de passe. Cliquez sur le lien ci-dessous pour le modifier :\n"
                    + resetLink + "\n\n"
                    + "Ce lien est valide pendant 1 heure.\n\n"
                    + "Si vous n'avez pas fait cette demande, ignorez ce message.\n\n"
                    + "Cordialement,\nVotre équipe.");

            mailSender.send(message);
            logger.info("Email de réinitialisation envoyé avec succès à : {}", toEmail);

        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email à : {}", toEmail, e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }


}