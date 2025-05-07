package sf.mephi.study.otp.service;

import sf.mephi.study.otp.dao.OTPConfigDAO;
import sf.mephi.study.otp.dao.UserDAO;
import sf.mephi.study.otp.model.OTPConfig;
import sf.mephi.study.otp.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private final UserDAO userDAO;
    private final OTPConfigDAO otpConfigDAO;

    public AdminService(UserDAO userDAO, OTPConfigDAO otpConfigDAO) {
        this.userDAO = userDAO;
        this.otpConfigDAO = otpConfigDAO;
    }

    public void deleteUser(String login) {
        userDAO.delete(login);
        logger.debug("User with login {} deleted successfully", login);
    }

    public List<User> getAllUsers() {
        List<User> users = userDAO.findAll();
        logger.debug("Retrieved {} users", users.size());
        return users;
    }

    public void updateOTPConfig(int codeLength, int expirationTime) {
        OTPConfig config = new OTPConfig(0, codeLength, expirationTime);
        otpConfigDAO.saveOrUpdateConfig(config);
        logger.debug("OTP config updated: codeLength={}, expirationTime={}", codeLength, expirationTime);
    }

    public Optional<OTPConfig> getOTPConfig() {
        Optional<OTPConfig> config = otpConfigDAO.getConfig();
        if (config.isPresent()) {
            logger.debug("OTP config retrieved: codeLength={}, expirationTime={}",
                    config.get().getCodeLength(), config.get().getExpirationTime());
        } else {
            logger.warn("OTP config not found");
        }
        return config;
    }

    public Optional<User> getUserByLogin(String login) {
        Optional<User> user = userDAO.findByLogin(login);
        if (user.isPresent()) {
            logger.debug("User retrieved by login: {}", login);
        } else {
            logger.warn("User not found for login: {}", login);
        }
        return user;
    }
}
