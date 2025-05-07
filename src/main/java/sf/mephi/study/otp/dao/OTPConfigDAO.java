package sf.mephi.study.otp.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sf.mephi.study.otp.model.OTPConfig;
import sf.mephi.study.otp.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class OTPConfigDAO {

    private static final Logger logger = LoggerFactory.getLogger(OTPConfigDAO.class);

    public Optional<OTPConfig> getConfig() {
        String sql = "SELECT id, code_length, expiration_time FROM otp_config LIMIT 1";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                int codeLength = resultSet.getInt("code_length");
                int expirationTime = resultSet.getInt("expiration_time");
                return Optional.of(new OTPConfig(id, codeLength, expirationTime));
            }
        } catch (SQLException e) {
            logger.error("SQL Error: ", e);
        }
        return Optional.empty();
    }

    public void saveOrUpdateConfig(OTPConfig config) {
        Optional<OTPConfig> existingConfig = getConfig();
        if (existingConfig.isPresent()) {
            updateConfig(config);
            logger.debug("OTP config has been updated");
        } else {
            insertConfig(config);
            logger.debug("OTP config has been created");
        }
    }

    private void insertConfig(OTPConfig config) {
        String sql = "INSERT INTO otp_config (code_length, expiration_time) VALUES (?, ?)";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, config.getCodeLength());
            statement.setInt(2, config.getExpirationTime());
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("SQL Error: ", e);
        }
    }

    private void updateConfig(OTPConfig config) {
        String sql = "UPDATE otp_config SET code_length = ?, expiration_time = ?";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, config.getCodeLength());
            statement.setInt(2, config.getExpirationTime());
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("SQL Error: ", e);
        }
    }
}
