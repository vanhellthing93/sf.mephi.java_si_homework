package sf.mephi.study.otp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sf.mephi.study.otp.dao.UserDAO;
import sf.mephi.study.otp.model.User;
import sf.mephi.study.otp.util.EncryptionUtil;

import java.util.Optional;

public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
        logger.debug("UserService initialized with UserDAO");
    }

    public void registerUser(String login, String password, User.Role role) {
        logger.debug("Attempting to register user: {}", login);
        try {
            String salt = EncryptionUtil.generateSalt();
            String encryptedPassword = EncryptionUtil.hashPassword(password, salt);
            User user = new User(login, encryptedPassword, salt, role);
            userDAO.save(user);
            logger.debug("User registered successfully: {}", login);
        } catch (Exception e) {
            logger.error("Failed to register user: {}", login, e);
            throw e; // (Опционально) Можно пробросить исключение дальше или обработать
        }
    }

    public Optional<User> getUserByLogin(String login) {
        logger.debug("Searching for user by login: {}", login);
        Optional<User> user = userDAO.findByLogin(login);
        if (user.isPresent()) {
            logger.debug("Found user: {}", login);
        } else {
            logger.debug("User not found: {}", login);
        }
        return user;
    }

    public Optional<User> getUserByRole(String role) {
        logger.debug("Searching for user by role: {}", role);
        Optional<User> user = userDAO.findByRole(role);
        if (user.isPresent()) {
            logger.debug("Found user with role: {}", role);
        } else {
            logger.debug("No user found with role: {}", role);
        }
        return user;
    }

    public boolean authenticateUser(String login, String password) {
        logger.debug("Authenticating user: {}", login);
        Optional<User> userOptional = userDAO.findByLogin(login);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            boolean isAuthenticated = EncryptionUtil.verifyPassword(password, user.getEncryptedPassword(), user.getSalt());
            if (isAuthenticated) {
                logger.debug("User authenticated successfully: {}", login);
            } else {
                logger.warn("Authentication failed for user: {}", login);
            }
            return isAuthenticated;
        }
        logger.warn("User not found during authentication: {}", login);
        return false;
    }
}