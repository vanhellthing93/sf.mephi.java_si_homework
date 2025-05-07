package sf.mephi.study.otp.util;

import sf.mephi.study.otp.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

public class DatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);

    // SQL для создания таблиц
    private static final String CREATE_USERS_TABLE = """
        CREATE TABLE IF NOT EXISTS users (
            id SERIAL PRIMARY KEY,
            login VARCHAR(50) UNIQUE NOT NULL,
            encrypted_password VARCHAR(255) NOT NULL,
            role VARCHAR(10) CHECK (role IN ('ADMIN', 'USER')) NOT NULL,
            salt VARCHAR(255) NOT NULL
        )
        """;

    private static final String CREATE_OTP_CONFIG_TABLE = """
        CREATE TABLE IF NOT EXISTS otp_config (
            id SERIAL PRIMARY KEY,
            code_length INT NOT NULL,
            expiration_time INT NOT NULL
        )
        """;

    private static final String CREATE_OTP_CODES_TABLE = """
        CREATE TABLE IF NOT EXISTS otp_codes (
            id SERIAL PRIMARY KEY,
            operation_id VARCHAR(10) NOT NULL,
            user_login VARCHAR(50) NOT NULL,
            code VARCHAR(10) NOT NULL,
            status VARCHAR(10) CHECK (status IN ('ACTIVE', 'EXPIRED', 'USED')) NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (user_login) REFERENCES users(login) ON DELETE CASCADE
        )
        """;

    private static final String INITIAL_OTP_CONFIG = """
        INSERT INTO otp_config (code_length, expiration_time) 
        VALUES (6, 300)
        ON CONFLICT DO NOTHING
        """;

    public static Connection getConnection() throws SQLException {
        String url = AppConfig.getDbUrl();
        String user = AppConfig.getDbUser();
        String password = AppConfig.getDbPassword();

        Connection connection = DriverManager.getConnection(url, user, password);
        initializeDatabase(connection);
        return connection;
    }

    private static void initializeDatabase(Connection connection) throws SQLException {
        try {
            // Проверяем существование основной таблицы
            if (!tableExists(connection, "users")) {
                logger.info("Creating database tables...");
                createTables(connection);
                initializeData(connection);
                logger.info("Database initialized successfully");
            }
        } catch (SQLException e) {
            logger.error("Database initialization failed", e);
            throw e;
        }
    }

    private static boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private static void createTables(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(CREATE_USERS_TABLE);
            stmt.executeUpdate(CREATE_OTP_CONFIG_TABLE);
            stmt.executeUpdate(CREATE_OTP_CODES_TABLE);
        }
    }

    private static void initializeData(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(INITIAL_OTP_CONFIG);
        }
    }
}