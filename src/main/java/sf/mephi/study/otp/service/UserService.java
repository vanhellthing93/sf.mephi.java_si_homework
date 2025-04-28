package sf.mephi.study.otp.service;

import sf.mephi.study.otp.dao.UserDAO;
import sf.mephi.study.otp.model.User;
import sf.mephi.study.otp.util.EncryptionUtil;

import java.util.Optional;

public class UserService {

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void registerUser(String login, String password, User.Role role) {
        String salt = EncryptionUtil.generateSalt();
        String encryptedPassword = EncryptionUtil.hashPassword(password, salt);
        User user = new User(login, encryptedPassword, salt, role);
        userDAO.save(user);
    }

    public Optional<User> getUserByLogin(String login) {
        return userDAO.findByLogin(login);
    }

    public boolean authenticateUser(String login, String password) {
        Optional<User> userOptional = userDAO.findByLogin(login);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return EncryptionUtil.verifyPassword(password, user.getEncryptedPassword(), user.getSalt());
        }
        return false;
    }
}