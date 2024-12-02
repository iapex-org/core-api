package com.iapex.services.email;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.iapex.models.user.UserWeb;
import com.iapex.repositories.user.UserWebRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityNotFoundException;

@Service
public class WebEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private UserWebRepository userWebRepository;

    public String sendPasswordResetEmail(UserWeb userWeb) throws MessagingException {
        String verificationCode = generateVerificationCode();
        String resetPasswordUrl = "http://localhost:4200/auth/restore-password?code=" + verificationCode;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String htmlBody = "<!DOCTYPE html>\n" +
                    "<html lang=\"es\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <title>Restablecimiento de contraseña</title>\n" +
                    "</head>\n" +
                    "<body style=\"margin: 0; padding: 0; font-family: 'Poppins', sans-serif; background-color: #f9f9f9;\">\n"
                    +
                    "    <div style=\"max-width: 600px; margin: 20px auto; padding: 20px; background-color: #fff; border-radius: 10px; text-align: center;\">\n"
                    +
                    "        <div style=\"background-color: #1F89EA; text-align: center; padding: 20px; border-top-left-radius: 10px; border-top-right-radius: 10px; margin-bottom: 15px;\">\n"
                    +
                    "            <img src=\"https://i.ibb.co/G7YSNXC/encuentrame-white.png\" alt=\"Encuéntrame\" style=\"max-width: 200px;\">\n"
                    +
                    "        </div>\n" +
                    "        <img src=\"https://i.ibb.co/C83G5js/lock.png\" width=\"180px\" alt=\"Reestablecer contraseña\" style=\"margin-top: 20px;\">\n"
                    +
                    "        <h1 style=\"font-size: 24px; margin-bottom: 25px;\">Reestablezca su contraseña</h1>\n" +
                    "        <p style=\"margin-bottom: 20px; line-height: 1.6;\">Hola, " + userWeb.getUsername()
                    + ".<br> Ha solicitado restablecer su contraseña. Haga clic en el siguiente enlace para continuar con el proceso:</p>\n"
                    +
                    "        <a href=\"" + resetPasswordUrl
                    + "\" style=\"display: inline-block; margin: 10px auto; padding: 15px 30px; background-color: #1F89EA; color: #ffffff; font-size: 16px; text-decoration: none; border-radius: 10px;\">Restablecer contraseña</a>\n"
                    +
                    "            <p><b>Nota:</b> Si no reconoce este correo o no recuerda haber solicitado reestablecer su contraseña, ignore este correo.</p>\n"
                    +
                    "        <div style=\"background-color: #dddddd; padding: 10px 20px; margin-top: 15px; border-bottom-left-radius: 10px; border-bottom-right-radius: 10px; color: #525252; text-align: center;\">\n"
                    +
                    "            <p>Atentamente, el equipo de Encuéntrame. Todos los derechos reservados | © 2024</p>\n"
                    +
                    "        </div>\n" +
                    "    </div>\n" +
                    "</body>\n" +
                    "</html>";

            helper.setFrom("iapex@gmail.com");
            helper.setTo(userWeb.getEmail());
            helper.setSubject("Restablecimiento de Contraseña - Encuéntrame");
            helper.setText(htmlBody, true);

            mailSender.send(message);

            Cache verificationCodesCache = cacheManager.getCache("verificationCodes");
            if (verificationCodesCache != null) {
                verificationCodesCache.put(userWeb.getEmail(), verificationCode);
            }

            Cache codeToEmailCache = cacheManager.getCache("codeToEmailCache");
            if (codeToEmailCache != null) {
                codeToEmailCache.put(verificationCode, userWeb.getEmail());
            }

            return verificationCode;
        } catch (MessagingException | MailSendException e) {
            throw new MessagingException("Error al enviar el correo electrónico de verificación: " + e.getMessage());
        }
    }

    @Async("taskExecutor")
    public CompletableFuture<String> sendVerificationEmailAsync(UserWeb userWeb) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return sendVerificationEmail(userWeb);
            } catch (MessagingException e) {
                throw new CompletionException(e);
            }
        });
    }

    public String sendVerificationEmail(UserWeb userWeb) throws MessagingException {
        String verificationCode = generateVerificationCode();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String htmlBody = "<!DOCTYPE html>\n" +
                    "<html lang=\"es\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <title>Verificación de correo</title>\n" +
                    "</head>\n" +
                    "<body style=\"margin: 0; padding: 0; font-family: 'Poppins', sans-serif; background-color: #f9f9f9;\">\n"
                    +
                    "    <div style=\"max-width: 600px; margin: 20px auto; padding: 20px; background-color: #fff; border-radius: 10px; text-align: center;\">\n"
                    +
                    "        <div style=\"background-color: #1F89EA; text-align: center; padding: 20px; border-top-left-radius: 10px; border-top-right-radius: 10px; margin-bottom: 15px;\">\n"
                    +
                    "            <img src=\"https://i.ibb.co/G7YSNXC/encuentrame-white.png\" alt=\"Encuéntrame\" style=\"max-width: 200px;\">\n"
                    +
                    "        </div>\n" +
                    "        <img src=\"https://i.ibb.co/PcNxsy8/verify-email.png\" width=\"130px\" alt=\"Verificar e-mail\" style=\"margin-top: 20px;\">\n"
                    +
                    "        <h1 style=\"color: #333;\">Verifique su correo</h1>\n" +
                    "        <p style=\"margin-bottom: 20px; line-height: 1.6; color: #555;\">Hola, "
                    + userWeb.getUsername()
                    + ". <br>Tu dirección de correo electrónico ha sido registrada en tu cuenta de Encuéntrame. Para continuar, ingrese el código de seis dígitos mostrado a continuación en la página de verificación de correo.</p>\n"
                    +
                    "        <div style=\"text-align: center;\">\n" +
                    "            <span style=\"display: inline-block; margin: 0 5px; padding: 15px; font-size: 20px; color: #ffffff; border-radius: 5px; width: 40px; background-color: #1F89EA;\">"
                    + verificationCode.charAt(0) + "</span>\n" +
                    "            <span style=\"display: inline-block; margin: 0 5px; padding: 15px; font-size: 20px; color: #ffffff; border-radius: 5px; width: 40px; background-color: #1F89EA;\">"
                    + verificationCode.charAt(1) + "</span>\n" +
                    "            <span style=\"display: inline-block; margin: 0 5px; padding: 15px; font-size: 20px; color: #ffffff; border-radius: 5px; width: 40px; background-color: #1F89EA;\">"
                    + verificationCode.charAt(2) + "</span>\n" +
                    "            <span style=\"display: inline-block; margin: 0 5px; padding: 15px; font-size: 20px; color: #ffffff; border-radius: 5px; width: 40px; background-color: #1F89EA;\">"
                    + verificationCode.charAt(3) + "</span>\n" +
                    "            <span style=\"display: inline-block; margin: 0 5px; padding: 15px; font-size: 20px; color: #ffffff; border-radius: 5px; width: 40px; background-color: #1F89EA;\">"
                    + verificationCode.charAt(4) + "</span>\n" +
                    "            <span style=\"display: inline-block; margin: 0 5px; padding: 15px; font-size: 20px; color: #ffffff; border-radius: 5px; width: 40px; background-color: #1F89EA;\">"
                    + verificationCode.charAt(5) + "</span>\n" +
                    "        </div>\n" +
                    "        <div style=\"border-bottom: 1px solid #dddddd; margin: 20px 0;\"></div>\n" +
                    "        <div style=\"margin: 25px 0; font-size: 14px; color: #555555; text-align: center;\">\n" +
                    "            <p><b>Nota:</b> Si no reconoce este correo o no recuerda haberlo solicitado, ignore este mensaje.</p>\n"
                    +
                    "        </div>\n" +
                    "        <div style=\"background-color: #dddddd; padding: 10px 20px; margin-top: 15px; border-bottom-left-radius: 10px; border-bottom-right-radius: 10px; color: #525252; text-align: center;\">\n"
                    +
                    "            <p>Atentamente, el equipo de Encuéntrame. Todos los derechos reservados | © 2024</p>\n"
                    +
                    "        </div>\n" +
                    "    </div>\n" +
                    "</body>\n" +
                    "</html>";

            helper.setFrom("iapex@gmail.com");
            helper.setTo(userWeb.getEmail());
            helper.setSubject("Verficación de Correo Electrónico - Encuéntrame");
            helper.setText(htmlBody, true);
            mailSender.send(message);

            Cache verificationCodesCache = cacheManager.getCache("verificationCodes");
            if (verificationCodesCache != null) {
                verificationCodesCache.put(userWeb.getEmail(), verificationCode);
            }

            Cache codeToEmailCache = cacheManager.getCache("codeToEmailCache");
            if (codeToEmailCache != null) {
                codeToEmailCache.put(verificationCode, userWeb.getEmail());
            }

            return verificationCode;
        } catch (MessagingException | MailSendException e) {
            throw new MessagingException("Error al enviar el correo electrónico de verificación: " + e.getMessage());
        }
    }

    @Async("taskExecutor")
    public CompletableFuture<String> sendPasswordResetEmailAsync(UserWeb userWeb) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return sendPasswordResetEmail(userWeb);
            } catch (MessagingException e) {
                throw new CompletionException(e);
            }
        });
    }

    // OBTENER EL CORREO ELECTRÓNICO ASOCIADO CON UN CÓDIGO DE VERIFICACIÓN
    public String getEmailForVerificationCode(String code) {
        Cache codeToEmailCache = cacheManager.getCache("codeToEmailCache");
        return codeToEmailCache != null ? codeToEmailCache.get(code, String.class) : null;
    }

    // VALIDAR EL CÓDIGO DE VERIFICACIÓN
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

    // VERIFICAR LA INSTITUCIÓN DEL USUARIO CON EL CÓDIGO
    public void verifyUserWebWithCode(String verificationCode) throws Exception {
        Cache codeToEmailCache = cacheManager.getCache("codeToEmailCache");
        if (codeToEmailCache == null) {
            throw new Exception("Cache de verificación no disponible");
        }

        String email = codeToEmailCache.get(verificationCode, String.class);
        if (email == null) {
            throw new Exception("Código de verificación no válido o expirado.");
        }

        UserWeb userWeb = userWebRepository.findByEmail(email)
                .orElseThrow(
                        () -> new EntityNotFoundException("Usuario no encontrado con el email asociado al código."));

        Cache verificationCache = cacheManager.getCache("verificationCodes");
        if (verificationCache == null) {
            throw new Exception("Cache de verificación no disponible");
        }

        String cachedCode = verificationCache.get(email, String.class);
        if (!verificationCode.equals(cachedCode)) {
            throw new Exception("Código de verificación no válido.");
        }

        userWeb.setAccountVerified(true);
        userWebRepository.save(userWeb);

        verificationCache.evict(email);
        codeToEmailCache.evict(verificationCode);
    }

    // REENVIAR EL CÓDIGO DE VERIFICACIÓN
    @Async("taskExecutor")
    public CompletableFuture<String> resendVerificationCode(String email) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                UserWeb userWeb = userWebRepository.findByEmail(email)
                        .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con el email: " + email));

                if (userWeb.isAccountVerified()) {
                    throw new IllegalStateException("La cuenta ya está verificada");
                }
                return sendVerificationEmail(userWeb);
            } catch (MessagingException e) {
                throw new CompletionException(e);
            }
        });
    }

    // MÉTODO PARA ENVIAR CORREO ELECTRÓNICO
    public void sendEmailInstitution(String from, String body) throws MessagingException {
        try {
            // CREAR EL MENSAJE MIME
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // CONFIGURAR LOS DETALLES DEL CORREO ELECTRÓNICO
            helper.setFrom("ayuda@iapex.com");
            helper.setTo("iapex6500@gmail.com");
            String subject = "Mensaje enviado por " + from + " desde ayuda@iapex.com";
            helper.setSubject(subject);
            helper.setText(body, true);

            // ENVIAR EL CORREO ELECTRÓNICO
            mailSender.send(message);
        } catch (EntityNotFoundException e) {
            // LANZAR EXCEPCIÓN SI EL CORREO NO ES ENCONTRADO
            throw new IllegalArgumentException(
                    "Correo electrónico no encontrado. Por favor, asegúrate de usar tu correo electrónico registrado.",
                    e);
        } catch (MessagingException | MailSendException e) {
            // LANZAR EXCEPCIÓN SI HAY UN ERROR AL ENVIAR EL CORREO
            throw new MessagingException("Error al enviar el correo electrónico: " + e.getMessage(), e);
        }
    }

    // GENERAR CODIGO DE 6 CIFRAS
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}