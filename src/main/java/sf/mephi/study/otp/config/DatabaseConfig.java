package sf.mephi.study.otp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {

    private static Properties properties = new Properties();

    static {
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find database.properties");
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getUrl() {
        return properties.getProperty("db.url");
    }

    public static String getUser() {
        return properties.getProperty("db.user");
    }

    public static String getPassword() {
        return properties.getProperty("db.password");
    }
}