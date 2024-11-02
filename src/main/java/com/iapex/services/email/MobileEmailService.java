package com.iapex.services.email;

import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.iapex.models.user.UserMobile;
import com.iapex.repositories.user.UserMobileRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityNotFoundException;

@Service
public class MobileEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private CacheManager cacheManager;
    
    @Autowired
    private UserMobileRepository userMobileRepository;

    private static final String IMAGE_URL = "https://medexlaboratories.com/wp-content/uploads/2022/03/healthcare.png";

    public String sendPasswordResetEmail(UserMobile userMobile) throws MessagingException {
        String verificationCode = generateVerificationCode();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String htmlBody = "<!DOCTYPE html>\n" +
                    "<html lang=\"es\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, intial-scale=1.0\">\n" +
                    "    <title>Restablecimiento de contraseña</title>\n" +
                    "</head>\n" +
                    "<body style=\"font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: transparent !important;\">\n" +
                    "    <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff;\">\n" +
                    "        <tr>\n" +
                    "            <td style=\"background-color: #0077b6; color: white; padding: 10px; text-align: center;\">\n" +
                    "                <h1 style=\"margin: 0;\">IAPEX</h1>\n" +
                    "                <p style=\"margin: 5px 0 0 0;\">Inteligencia artificial para la búsqueda de pacientes extraviados en instituciones de salud</p>\n" +
                    "            </td>\n" +
                    "        </tr>\n" +
                    "        <tr>\n" +
                    "            <td style=\"padding: 20px;\">\n" +
                    "                <img src=\"" + IMAGE_URL + "\" alt=\"IAPEX Logo\" style=\"max-width: 200px; height: auto; display: block; margin: 0 auto 20px;\">\n" +
                    "                <h2 style=\"text-align: center;\">Restablecimiento de contraseña</h2>\n" +
                    "                <p>Has solicitado restablecer tu contraseña. Utiliza el siguiente código para completar el proceso:</p>\n" +
                    "                <h3 style=\"text-align: center;\">Tu código de verificación es: " + verificationCode + "</h3>\n" +
                    "                <p>Si no has solicitado este cambio, por favor ignora este correo o contacta con soporte.</p>\n" +
                    "            </td>\n" +
                    "        </tr>\n" +
                    "        <tr>\n" +
                    "            <td style=\"color: #888888; text-align: center; font-size: 12px;\">\n" +
                    "                <p>Si tienes alguna pregunta, no dudes en contactarnos.</p>\n" +
                    "                <p>© 2024 IAPEX. Todos los derechos reservados.</p>\n" +
                    "            </td>\n" +
                    "        </tr>\n" +
                    "    </table>\n" +
                    "</body>\n" +
                    "</html>";

            helper.setFrom("iapex@gmail.com");
            helper.setTo(userMobile.getEmail());
            helper.setSubject("Restablecimiento de Contraseña en IAPEX");
            helper.setText(htmlBody, true);

            mailSender.send(message);

            cacheManager.getCache("verificationCodes").put(userMobile.getEmail(), verificationCode);
            cacheManager.getCache("codeToEmailCache").put(verificationCode, userMobile.getEmail());

            return verificationCode;
        } catch (MessagingException | MailSendException e) {
            throw new MessagingException("Error al enviar el correo electrónico de verificación: " + e.getMessage());
        }
    }
    
    public String sendVerificationEmail(UserMobile userMobile) throws MessagingException {
        String verificationCode = generateVerificationCode();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String htmlBody = "<!DOCTYPE html>\n" +
                    "<html lang=\"es\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <title>Confirmación de registro</title>\n" +
                    "</head>\n" +
                    "<body style=\"font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: transparent !important;\">\n" +
                    "    <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff;\">\n" +
                    "        <tr>\n" +
                    "            <td style=\"background-color: #0077b6; color: white; padding: 10px; text-align: center;\">\n" +
                    "                <h1 style=\"margin: 0;\">IAPEX</h1>\n" +
                    "                <p style=\"margin: 5px 0 0 0;\">Inteligencia artificial para la búsqueda de pacientes extraviados en instituciones de salud</p>\n" +
                    "            </td>\n" +
                    "        </tr>\n" +
                    "        <tr>\n" +
                    "            <td style=\"padding: 20px;\">\n" +
                    "                <img src=\"" + IMAGE_URL + "\" alt=\"IAPEX Logo\" style=\"max-width: 200px; height: auto; display: block; margin: 0 auto 20px;\">\n" +
                    "                <h2 style=\"text-align: center;\">Gracias por registrarte. Confirma tu correo electrónico para confirmar tu cuenta.</h2>\n" +
                    "                <p>Una vez que tu cuenta haya sido confirmada, podrás acceder a todos los servicios de IAPEX.</p>\n" +
                    "                <h3 style=\"text-align: center;\">Tu código de verificación es: " + verificationCode + "</h3>\n" +
                    "            </td>\n" +
                    "        </tr>\n" +
                    "        <tr>\n" +
                    "            <td style=\"color: #888888; text-align: center; font-size: 12px;\">\n" +
                    "                <p>Si tienes alguna pregunta, no dudes en contactarnos.</p>\n" +
                    "                <p>© 2024 IAPEX. Todos los derechos reservados.</p>\n" +
                    "            </td>\n" +
                    "        </tr>\n" +
                    "    </table>\n" +
                    "</body>\n" +
                    "</html>";

            helper.setFrom("iapex@gmail.com");
            helper.setTo(userMobile.getEmail());
            helper.setSubject("Confirmación de Registro en IAPEX");
            helper.setText(htmlBody, true);

            mailSender.send(message);

            cacheManager.getCache("verificationCodes").put(userMobile.getEmail(), verificationCode);
            cacheManager.getCache("codeToEmailCache").put(verificationCode, userMobile.getEmail());

            return verificationCode;
        } catch (MessagingException | MailSendException e) {
            throw new MessagingException("Error al enviar el correo electrónico de verificación: " + e.getMessage());
        }
    }
    
    public String getEmailForVerificationCode(String code) {
        Cache codeToEmailCache = cacheManager.getCache("codeToEmailCache");
        return codeToEmailCache != null ? codeToEmailCache.get(code, String.class) : null;
    }
    
    public boolean verifyCode(String code) {
        String email = getEmailForVerificationCode(code);
        if (email != null) {
            Cache verificationCodesCache = cacheManager.getCache("verificationCodes");
            Cache codeToEmailCache = cacheManager.getCache("codeToEmailCache");

            if (verificationCodesCache != null && codeToEmailCache != null) {
                String storedCode = verificationCodesCache.get(email, String.class);
                if (storedCode != null && storedCode.equals(code)) {
                    verificationCodesCache.evict(email);
                    codeToEmailCache.evict(code);
                    return true;
                }
            }
        }
        return false;
    }
    
    public void verifyUserWithCode(String email, String verificationCode) throws Exception {
    	UserMobile userMobile = userMobileRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con el email: " + email));

        Cache verificationCache = cacheManager.getCache("verificationCodes");
        if (verificationCache == null) {
            throw new Exception("Cache de verificación no disponible");
        }

        String cachedCode = verificationCache.get(email, String.class);
        if (cachedCode == null || !cachedCode.equals(verificationCode)) {
            throw new Exception("Código de verificación no válido.");
        }

        userMobile.setStatus(true);
        userMobileRepository.save(userMobile);

        verificationCache.evict(email);
        cacheManager.getCache("codeToEmailCache").evict(verificationCode);
    }
    
    public String resendVerificationCode(String email) throws MessagingException {
    	UserMobile userMobile = userMobileRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con el email: " + email));

        if (userMobile.isStatus()) {
            throw new IllegalStateException("La cuenta ya está verificada");
        }

        return sendVerificationEmail(userMobile);
    }
    
    public void sendEmail(String from, String body) throws MessagingException {
        try {
        	MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("ayuda@iapex.com");
            helper.setTo("iapex6500@gmail.com");
            String subject = "Mensaje enviado por " + from + " desde ayuda@iapex.com";
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
        } catch (EntityNotFoundException e) {
            throw new IllegalArgumentException("Correo electrónico no encontrado. Por favor, asegúrate de usar tu correo electrónico registrado.", e);
        } catch (MessagingException | MailSendException e) {
            throw new MessagingException("Error al enviar el correo electrónico: " + e.getMessage(), e);
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}