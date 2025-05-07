package sf.mephi.study.otp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static Properties appProperties = new Properties();

    static {
        loadProperties("application.properties", appProperties);
    }

    private static void loadProperties(String fileName, Properties properties) {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                String errorMessage = "Unable to find " + fileName;
                logger.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
            properties.load(input);
            logger.info("Properties loaded successfully from {}", fileName);
        } catch (IOException ex) {
            logger.error("Failed to load properties from {}", fileName, ex);
        }
    }

    public static String getJwtSecret() {
        return getProperty("jwt.secret");
    }

    public static long getJwtExpirationTime() {
        return Long.parseLong(getProperty("jwt.expiration.time"));
    }

    public static String getDbUrl() {
        return getProperty("db.url");
    }

    public static String getDbUser() {
        return getProperty("db.user");
    }

    public static String getDbPassword() {
        return getProperty("db.password");
    }

    public static String getEmailUsername() {
        return getProperty("email.username");
    }

    public static String getEmailPassword() {
        return getProperty("email.password");
    }

    public static String getEmailFrom() {
        return getProperty("email.from");
    }

    public static String getSmtpHost() {
        return getProperty("mail.smtp.host");
    }

    public static int getSmtpPort() {
        return Integer.parseInt(getProperty("mail.smtp.port"));
    }

    public static boolean isSmtpAuth() {
        return Boolean.parseBoolean(getProperty("mail.smtp.auth"));
    }

    public static boolean isSmtpSslEnable() {
        return Boolean.parseBoolean(getProperty("mail.smtp.ssl.enable"));
    }

    public static String getSmppHost() {
        return getProperty("smpp.host");
    }

    public static int getSmppPort() {
        return Integer.parseInt(getProperty("smpp.port"));
    }

    public static String getSmppSystemId() {
        return getProperty("smpp.system_id");
    }

    public static String getSmppPassword() {
        return getProperty("smpp.password");
    }

    public static String getSmppSystemType() {
        return getProperty("smpp.system_type");
    }

    public static String getSmppSourceAddr() {
        return getProperty("smpp.source_addr");
    }

    public static String getTelegramBotToken() {
        return getProperty("telegram.bot.token");
    }

    public static String getTelegramChatId() {
        return getProperty("telegram.chat.id");
    }

    public static String getOtpFilePath() {
        return "otp_codes.txt";
    }

    public static long getOtpExpirationTime() {
        return Long.parseLong(getProperty("otp.expiration.time"));
    }

    private static String getProperty(String key) {
        String value = appProperties.getProperty(key);
        if (value == null) {
            logger.warn("Property {} is not set in the configuration", key);
        }
        return value;
    }
}
