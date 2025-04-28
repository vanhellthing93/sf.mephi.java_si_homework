package sf.mephi.study.otp.dao;

import sf.mephi.study.otp.model.User;
import sf.mephi.study.otp.util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public Optional<User> findByLogin(String login) {
        String sql = "SELECT login, encrypted_password, salt, role FROM users WHERE login = ?";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String encryptedPassword = resultSet.getString("encrypted_password");
                String salt = resultSet.getString("salt");
                User.Role role = User.Role.valueOf(resultSet.getString("role"));
                return Optional.of(new User(login, encryptedPassword, salt, role));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void save(User user) {
        String sql = "INSERT INTO users (login, encrypted_password, role, salt) VALUES (?, ?, ?, ?)";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getLogin());
            statement.setString(2, user.getEncryptedPassword());
            statement.setString(3, user.getRole().name());
            statement.setString(4, user.getSalt());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void delete(String login) {
        String sql = "DELETE FROM users WHERE login = ?";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, login);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT login, encrypted_password, salt, role FROM users";
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String login = resultSet.getString("login");
                String encryptedPassword = resultSet.getString("encrypted_password");
                String salt = resultSet.getString("salt");
                User.Role role = User.Role.valueOf(resultSet.getString("role"));
                users.add(new User(login, encryptedPassword, salt, role));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

}