package sf.mephi.study.otp.service;

import sf.mephi.study.otp.dao.UserDAO;
import sf.mephi.study.otp.model.User;

import java.util.Optional;

public class UserService {

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void registerUser(String login, String encryptedPassword, User.Role role) {
        User user = new User(login, encryptedPassword, role);
        userDAO.save(user);
    }

    public Optional<User> getUserByLogin(String login) {
        return userDAO.findByLogin(login);
    }

}