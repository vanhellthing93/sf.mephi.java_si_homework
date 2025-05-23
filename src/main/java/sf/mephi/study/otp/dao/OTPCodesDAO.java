package sf.mephi.study.otp.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sf.mephi.study.otp.model.OTPCode;
import sf.mephi.study.otp.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OTPCodesDAO {

    private static final Logger logger = LoggerFactory.getLogger(OTPCodesDAO.class);

    public void save(OTPCode otpCode) {
        String sql = "INSERT INTO otp_codes (operation_id, user_login, code, status, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, otpCode.getOperationId());
            statement.setString(2, otpCode.getUser());
            statement.setString(3, otpCode.getCode());
            statement.setString(4, otpCode.getStatus().name());
            statement.setTimestamp(5, Timestamp.valueOf(otpCode.getCreatedAt()));
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    otpCode.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            logger.error("SQL Error: ", e);
        }
    }

    public Optional<OTPCode> findActiveByOperationId(String operationId) {
        String sql = "SELECT id, operation_id, user_login, code, status, created_at FROM otp_codes WHERE operation_id = ? AND status = 'ACTIVE'";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, operationId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String code = resultSet.getString("code");
                String user = resultSet.getString("user_login");
                OTPCode.Status status = OTPCode.Status.valueOf(resultSet.getString("status"));
                LocalDateTime createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();
                return Optional.of(new OTPCode(id, operationId, code, status, createdAt, user));
            }
        } catch (SQLException e) {
            logger.error("SQL Error: ", e);
        }
        return Optional.empty();
    }

    public List<OTPCode> findAllByOperationId(String operationId) {
        String sql = "SELECT id, operation_id, user_login, code, status, created_at FROM otp_codes WHERE operation_id = ?";
        List<OTPCode> otpCodes = new ArrayList<>();
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, operationId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String code = resultSet.getString("code");
                String user = resultSet.getString("user_login");
                OTPCode.Status status = OTPCode.Status.valueOf(resultSet.getString("status"));
                LocalDateTime createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();
                otpCodes.add(new OTPCode(id, operationId, code, status, createdAt, user));
            }
        } catch (SQLException e) {
            logger.error("SQL Error: ", e);
        }
        return otpCodes;
    }

    public List<OTPCode> findAll() {
        List<OTPCode> otpCodes = new ArrayList<>();
        String sql = "SELECT id, operation_id, user_login, code, status, created_at FROM otp_codes";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String operationId = resultSet.getString("operation_id");
                String code = resultSet.getString("code");
                String user = resultSet.getString("user_login");
                OTPCode.Status status = OTPCode.Status.valueOf(resultSet.getString("status"));
                LocalDateTime createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();
                otpCodes.add(new OTPCode(id, operationId, code, status, createdAt, user));
            }
        } catch (SQLException e) {
            logger.error("SQL Error: ", e);
        }
        return otpCodes;
    }

    public void updateStatus(int id, OTPCode.Status status) {
        String sql = "UPDATE otp_codes SET status = ? WHERE id = ?";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());
            statement.setInt(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("SQL Error: ", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM otp_codes WHERE id = ?";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("SQL Error: ", e);
        }
    }
}
