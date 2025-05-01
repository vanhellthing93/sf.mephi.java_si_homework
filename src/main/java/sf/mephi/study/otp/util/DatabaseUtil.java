package sf.mephi.study.otp.util;

import sf.mephi.study.otp.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {

    public static Connection getConnection() throws SQLException {
        String url = AppConfig.getDbUrl();
        String user = AppConfig.getDbUser();
        String password = AppConfig.getDbPassword();

        return DriverManager.getConnection(url, user, password);
    }
}