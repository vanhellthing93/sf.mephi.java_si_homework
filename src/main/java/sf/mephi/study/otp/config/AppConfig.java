package sf.mephi.study.otp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    private static Properties appProperties = new Properties();
    private static Properties dbProperties = new Properties();
    private static Properties emailProperties = new Properties();
    private static Properties smsProperties = new Properties();
    private static Properties telegramProperties = new Properties();
    private static Properties fileProperties = new Properties();

    static {
        loadProperties("application.properties", appProperties);
        loadProperties("database.properties", dbProperties);
        loadProperties("email.properties", emailProperties);
        loadProperties("sms.properties", smsProperties);
        loadProperties("telegram.properties", telegramProperties);
        loadProperties("file.properties", fileProperties);

    }

    private static void loadProperties(String fileName, Properties properties) {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new RuntimeException("Unable to find " + fileName);
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getJwtSecret() {
        return appProperties.getProperty("jwt.secret");
    }

    public static long getExpirationTime() {
        String expirationTimeStr = appProperties.getProperty("jwt.expiration.time");
        return Long.parseLong(expirationTimeStr);
    }

    public static String getDbUrl() {
        return dbProperties.getProperty("db.url");
    }

    public static String getDbUser() {
        return dbProperties.getProperty("db.user");
    }

    public static String getDbPassword() {
        return dbProperties.getProperty("db.password");
    }

    public static String getEmailUsername() {
        return emailProperties.getProperty("email.username");
    }

    public static String getEmailPassword() {
        return emailProperties.getProperty("email.password");
    }

    public static String getEmailFrom() {
        return emailProperties.getProperty("email.from");
    }

    public static String getSmtpHost() {
        return emailProperties.getProperty("mail.smtp.host");
    }

    public static int getSmtpPort() {
        String portStr = emailProperties.getProperty("mail.smtp.port");
        return Integer.parseInt(portStr);
    }

    public static boolean isSmtpAuth() {
        String authStr = emailProperties.getProperty("mail.smtp.auth");
        return Boolean.parseBoolean(authStr);
    }

    public static boolean isSmtpSslEnable() {
        String sslEnableStr = emailProperties.getProperty("mail.smtp.ssl.enable");
        return Boolean.parseBoolean(sslEnableStr);
    }

    public static String getSmppHost() {
        return smsProperties.getProperty("smpp.host");
    }

    public static int getSmppPort() {
        String portStr = smsProperties.getProperty("smpp.port");
        return Integer.parseInt(portStr);
    }

    public static String getSmppSystemId() {
        return smsProperties.getProperty("smpp.system_id");
    }

    public static String getSmppPassword() {
        return smsProperties.getProperty("smpp.password");
    }

    public static String getSmppSystemType() {
        return smsProperties.getProperty("smpp.system_type");
    }

    public static String getSmppSourceAddr() {
        return smsProperties.getProperty("smpp.source_addr");
    }

    public static String getTelegramBotToken() {
        return telegramProperties.getProperty("telegram.bot.token");
    }

    public static String getTelegramChatId() {
        return telegramProperties.getProperty("telegram.chat.id");
    }

    public static String getOtpFilePath() {
        return fileProperties.getProperty("otp.file.path");
    }
}
