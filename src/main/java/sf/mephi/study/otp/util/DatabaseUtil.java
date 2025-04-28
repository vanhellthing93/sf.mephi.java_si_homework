package sf.mephi.study.otp.util;

import sf.mephi.study.otp.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {

    public static Connection getConnection() throws SQLException {
        String url = DatabaseConfig.getUrl();
        String user = DatabaseConfig.getUser();
        String password = DatabaseConfig.getPassword();

        return DriverManager.getConnection(url, user, password);
    }
}