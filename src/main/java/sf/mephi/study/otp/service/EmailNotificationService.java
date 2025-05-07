package sf.mephi.study.otp.service;

import sf.mephi.study.otp.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);
    private final String username;
    private final String password;
    private final String fromEmail;
    private final Session session;

    public EmailNotificationService() {
        // Загрузка конфигурации
        Properties config = loadConfig();
        this.username = config.getProperty("email.username");
        this.password = config.getProperty("email.password");
        this.fromEmail = config.getProperty("email.from");
        this.session = Session.getInstance(config, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        logger.debug("Email session initialized successfully");
    }

    private Properties loadConfig() {
        Properties config = new Properties();
        config.put("mail.smtp.host", AppConfig.getSmtpHost());
        config.put("mail.smtp.port", AppConfig.getSmtpPort());
        config.put("mail.smtp.auth", AppConfig.isSmtpAuth());
        config.put("mail.smtp.starttls.enable", AppConfig.isSmtpSslEnable());
        config.put("mail.smtp.ssl.enable", AppConfig.isSmtpSslEnable());
        config.put("email.username", AppConfig.getEmailUsername());
        config.put("email.password", AppConfig.getEmailPassword());
        config.put("email.from", AppConfig.getEmailFrom());
        logger.debug("Email configuration loaded successfully");
        return config;
    }

    public void sendCode(String toEmail, String code) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("Your OTP Code");
            message.setText("Your verification code is: " + code);

            Transport.send(message);
            logger.debug("Email message sent successfully to {}", toEmail);
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
