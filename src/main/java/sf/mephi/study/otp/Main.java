package sf.mephi.study.otp;

import sf.mephi.study.otp.dao.UserDAO;
import sf.mephi.study.otp.model.User;
import sf.mephi.study.otp.util.DatabaseUtil;
import sf.mephi.study.otp.util.EncryptionUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        System.out.println("Start program");


        // Создаем объект DAO для работы с пользователями
        UserDAO userDAO = new UserDAO();

        // Создаем нового пользователя
        String salt = EncryptionUtil.generateSalt();
        String encryptedPassword = EncryptionUtil.hashPassword("test", salt);
        User newUser = new User("test", encryptedPassword, salt, User.Role.USER);

        // Сохраняем пользователя в базу данных
        saveUser(newUser, userDAO);

        // Получаем пользователя из базы данных по логину
        Optional<User> retrievedUser = userDAO.findByLogin("test");

        // Выводим информацию о пользователе в консоль
        retrievedUser.ifPresentOrElse(
                user -> System.out.println("Retrieved User: " + user),
                () -> System.out.println("User not found")
        );
    }

    private static void saveUser(User user, UserDAO userDAO) {
        try (Connection connection = DatabaseUtil.getConnection()) {
            userDAO.save(user);
            System.out.println("User saved successfully");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to save user");
        }
    }
}