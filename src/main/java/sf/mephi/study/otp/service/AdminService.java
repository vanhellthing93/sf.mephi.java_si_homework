package sf.mephi.study.otp.service;

import sf.mephi.study.otp.dao.OTPConfigDAO;
import sf.mephi.study.otp.dao.UserDAO;
import sf.mephi.study.otp.model.OTPConfig;
import sf.mephi.study.otp.model.User;

import java.util.List;
import java.util.Optional;

public class AdminService {

    private final UserDAO userDAO;
    private final OTPConfigDAO otpConfigDAO;

    public AdminService(UserDAO userDAO, OTPConfigDAO otpConfigDAO) {
        this.userDAO = userDAO;
        this.otpConfigDAO = otpConfigDAO;
    }

    public void deleteUser(String login) {
        userDAO.delete(login);
    }

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public void updateOTPConfig(int codeLength, int expirationTime) {
        OTPConfig config = new OTPConfig(0, codeLength, expirationTime);
        otpConfigDAO.saveOrUpdateConfig(config);
    }

    public Optional<OTPConfig> getOTPConfig() {
        return otpConfigDAO.getConfig();
    }

    public Optional<User> getUserByLogin(String login) {
        return userDAO.findByLogin(login);
    }
}