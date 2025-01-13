package com.iapex.services.email;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
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

import com.iapex.models.ContactRequest;
import com.iapex.models.patient.Patient;
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
                    + ".<br>Su dirección de correo electrónico ha sido ingresada para solicitar restablecer su contraseña. Haga clic en el siguiente botón para continuar con el proceso:</p>\n"
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

    public String sendVerificationEmail(UserWeb userWeb) throws MessagingException {
        String verificationCode = generateVerificationCode();
        String verifyEmailUrl = "http://localhost:8080/api/v1/users/web/verify-email?code=" + verificationCode;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String htmlBody = "<!DOCTYPE html>\n" +
                    "<html lang=\"es\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
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
                    "        <h1 style=\"color: #333;\">Verifique su correo electrónico</h1>\n" +
                    "        <p style=\"margin-bottom: 20px; line-height: 1.6; color: #555;\">Hola, "
                    + userWeb.getUsername()
                    + ". <br>Su dirección de correo electrónico ha sido registrada en una cuenta de Encuéntrame. Haga clic en el siguiente botón para continuar con el proceso:</p>\n"
                    +
                    "        <a href=\"" + verifyEmailUrl
                    + "\" style=\"display: inline-block; margin: 10px auto; padding: 15px 30px; background-color: #1F89EA; color: #ffffff; font-size: 16px; text-decoration: none; border-radius: 10px;\">Verificar correo electrónico</a>\n"
                    +
                    "            <p><b>Nota:</b> Si no reconoce este correo o no recuerda haberlo solicitado, ignore este mensaje.</p>\n"
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

    @Async("taskExecutor")
    public void sendEmailsToWebUsers(ContactRequest contactRequest, List<UserWeb> users) {
        for (UserWeb user : users) {
            try {
                sendContactRequestEmail(contactRequest, user);
            } catch (MessagingException e) {
                // Manejar errores de envío de correo
                System.err.println("Error al enviar correo a " + user.getEmail() + ": " + e.getMessage());
            }
        }
    }

    public void sendContactRequestEmail(ContactRequest contactRequest, UserWeb user) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        // Formateador para la fecha y hora en español
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy 'a las' hh:mm a",
                new Locale("es", "ES"));
        String formattedRequestDate = contactRequest.getRequestDateTime().format(formatter);

        // Obtener nombre completo del paciente o ID si no está disponible
        String patientIdentifier;
        if (contactRequest.getPatient() != null) {
            Patient patient = contactRequest.getPatient();
            if (patient.getName() != null && !patient.getName().isEmpty()) {
                patientIdentifier = patient.getName();
                if (patient.getLastName() != null && !patient.getLastName().isEmpty()) {
                    patientIdentifier += " " + patient.getLastName();
                }
                if (patient.getSecondLastName() != null && !patient.getSecondLastName().isEmpty()) {
                    patientIdentifier += " " + patient.getSecondLastName();
                }
            } else {
                patientIdentifier = "con el ID: " + patient.getId();
            }
        } else {
            patientIdentifier = "Paciente no identificado";
        }

        // Obtener la información de contacto
        StringBuilder contactInfoBuilder = new StringBuilder();
        if (contactRequest.getPhoneNumber() != null && !contactRequest.getPhoneNumber().isEmpty()) {
            contactInfoBuilder.append("con el número ").append(contactRequest.getPhoneNumber());
        }
        if (contactRequest.getEmail() != null && !contactRequest.getEmail().isEmpty()) {
            if (contactInfoBuilder.length() > 0) {
                contactInfoBuilder.append(" y "); // Conjunción si ambos están disponibles
            }
            contactInfoBuilder.append("con el correo ").append(contactRequest.getEmail());
        }
        String contactInfo = contactInfoBuilder.toString();

        // Cuerpo del mensaje en texto corrido
        String notificationText = "Hemos recibido una nueva solicitud de contacto para el paciente " + patientIdentifier
                + ". El interesado es " + contactRequest.getInterestedPersonName()
                + ", " + contactInfo
                + ". La solicitud fue recibida el " + formattedRequestDate;

        String htmlBody = "<!DOCTYPE html>\n" +
                "<html lang=\"es\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Nueva solicitud de contacto</title>\n" +
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
                "        <img src=\"https://i.ibb.co/PcNxsy8/verify-email.png\" width=\"130px\" alt=\"Solicitud de contacto\" style=\"margin-top: 20px;\">\n"
                +
                "        <h1 style=\"color: #333;\">Nueva solicitud de contacto</h1>\n" +
                "        <p style=\"margin-bottom: 20px; line-height: 1.6; color: #555;\">Hola, " + user.getUsername()
                + ",</p>\n" +
                "        <p style=\"line-height: 1.6; color: #555; text-align: center;\">" + notificationText + "</p>\n"
                +
                "        <h3 style=\"margin-top: 20px; color: #333;\">Por favor, revise y gestione esta solicitud a la mayor brevedad posible.</h3>\n"
                +
                "        <a href=\"http://localhost:4200/dashboard/contact-requests/details/" + contactRequest.getId()
                + "\" style=\"display: inline-block; margin: 10px auto; padding: 15px 30px; background-color: #1F89EA; color: #ffffff; font-size: 16px; text-decoration: none; border-radius: 10px;\">Revisar solicitud</a>\n"
                +
                "        <div style=\"background-color: #dddddd; padding: 10px 20px; margin-top: 15px; border-bottom-left-radius: 10px; border-bottom-right-radius: 10px; color: #525252; text-align: center;\">\n"
                +
                "            <p>Atentamente, el equipo de Encuéntrame. Todos los derechos reservados | © 2024</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";

        helper.setFrom("iapex@gmail.com");
        helper.setTo(user.getEmail());
        helper.setSubject("Nueva Solicitud de Contacto Para el Paciente " + patientIdentifier + " - Encuéntrame");
        helper.setText(htmlBody, true);

        mailSender.send(message);
    }

    public void sendContactRequestAcknowledgementEmail(ContactRequest contactRequest) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        // Formateador para la fecha y hora en español
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy 'a las' hh:mm a",
                new Locale("es", "ES"));
        String formattedRequestDate = contactRequest.getRequestDateTime().format(formatter);

        // Obtener la información de contacto
        StringBuilder contactInfoBuilder = new StringBuilder();
        if (contactRequest.getPhoneNumber() != null && !contactRequest.getPhoneNumber().isEmpty()) {
            contactInfoBuilder.append("con el número ").append(contactRequest.getPhoneNumber());
        }
        if (contactRequest.getEmail() != null && !contactRequest.getEmail().isEmpty()) {
            if (contactInfoBuilder.length() > 0) {
                contactInfoBuilder.append(" y "); // Conjunción si ambos están disponibles
            }
            contactInfoBuilder.append("con el correo ").append(contactRequest.getEmail());
        }
        String contactInfo = contactInfoBuilder.toString();

        // Cuerpo del mensaje en texto corrido
        String notificationText = "Hemos recibido tu solicitud de contacto para "
                + contactRequest.getMissingPersonName()
                + ". A nombre de " + contactRequest.getInterestedPersonName()
                + ", " + contactInfo
                + ". Tu solicitud fue recibida el " + formattedRequestDate;

        String htmlBody = "<!DOCTYPE html>\n" +
                "<html lang=\"es\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Nueva solicitud de contacto</title>\n" +
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
                "        <img src=\"https://i.ibb.co/PcNxsy8/verify-email.png\" width=\"130px\" alt=\"Solicitud de contacto\" style=\"margin-top: 20px;\">\n"
                +
                "        <h1 style=\"color: #333;\">Nueva solicitud de contacto</h1>\n" +
                "        <p style=\"margin-bottom: 20px; line-height: 1.6; color: #555;\">Hola, "
                + contactRequest.getInterestedPersonName()
                + ",</p>\n" +
                "        <p style=\"line-height: 1.6; color: #555; text-align: center;\">" + notificationText + "</p>\n"
                +
                "        <h3 style=\"margin-top: 20px; color: #333;\">En breve, alguien del equipo atenderá tu solicitud y serás notificado.</h3>\n"
                +
                "        <a href=\"http://localhost:4200/dashboard/contact-requests/details/" + contactRequest.getId()
                + "\" style=\"display: inline-block; margin: 10px auto; padding: 15px 30px; background-color: #1F89EA; color: #ffffff; font-size: 16px; text-decoration: none; border-radius: 10px;\">Revisar solicitud</a>\n"
                +
                "        <div style=\"background-color: #dddddd; padding: 10px 20px; margin-top: 15px; border-bottom-left-radius: 10px; border-bottom-right-radius: 10px; color: #525252; text-align: center;\">\n"
                +
                "            <p>Atentamente, el equipo de Encuéntrame. Todos los derechos reservados | © 2024</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";

        helper.setFrom("iapex@gmail.com");
        helper.setTo(contactRequest.getEmail());
        helper.setSubject("Confirmación de Solicitud de Contacto - Encuéntrame");
        helper.setText(htmlBody, true);

        mailSender.send(message);
    }

    public void sendContactRequestStatusUpdateEmail(ContactRequest contactRequest) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        // Nombre completo del attendig user
        String attendingUserFullName = contactRequest.getAttendingUser().getName() + " "
                + contactRequest.getAttendingUser().getLastName() + " "
                + contactRequest.getAttendingUser().getSecondLastName();

        // Cuerpo del mensaje en texto corrido
        String notificationText = "Tu solicitud de contacto para " + contactRequest.getMissingPersonName()
                + " a nombre de " + contactRequest.getInterestedPersonName()
                + " se ha puesto en " + contactRequest.getStatus().toLowerCase().replace("_", " ")
                + ". El usuario " + attendingUserFullName
                + " será el encargado de darle seguimiento a tú solicitud.";

        String htmlBody = "<!DOCTYPE html>\n" +
                "<html lang=\"es\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Actualización en tu Solicitud de Contacto</title>\n" +
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
                "        <img src=\"https://i.ibb.co/PcNxsy8/verify-email.png\" width=\"130px\" alt=\"Solicitud de contacto\" style=\"margin-top: 20px;\">\n"
                +
                "        <h1 style=\"color: #333;\">Seguimiento a tu solicitud</h1>\n" +
                "        <p style=\"margin-bottom: 20px; line-height: 1.6; color: #555;\">Hola, "
                + contactRequest.getInterestedPersonName() + ",</p>\n" +
                "        <p style=\"line-height: 1.6; color: #555; text-align: center;\">" + notificationText + "</p>\n"
                +
                "        <h3 style=\"margin-top: 20px; color: #333;\">En breve, la persona encargada de dar seguimiento a tu solicitud se pondrá en contacto contigo." +
                "        <div style=\"background-color: #dddddd; padding: 10px 20px; margin-top: 15px; border-bottom-left-radius: 10px; border-bottom-right-radius: 10px; color: #525252; text-align: center;\">\n"
                +
                "            <p>Atentamente, el equipo de Encuéntrame. Todos los derechos reservados | © 2024</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";

        // Enviar el correo
        helper.setFrom("iapex@gmail.com");
        helper.setTo(contactRequest.getEmail()); // Correo del interesado
        helper.setSubject("Seguimiento a tu Solicitud de Contacto - Encuéntrame");
        helper.setText(htmlBody, true);

        mailSender.send(message);
    }

    // GENERAR CODIGO DE 6 CIFRAS
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}