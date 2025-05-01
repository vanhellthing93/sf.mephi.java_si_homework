package sf.mephi.study.otp.service;

import sf.mephi.study.otp.config.AppConfig;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;


public class EmailNotificationService {

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
            System.out.println("Email message sent successfully");
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}