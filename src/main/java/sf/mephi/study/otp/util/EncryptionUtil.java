package sf.mephi.study.otp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionUtil {

    private static final Logger logger = LoggerFactory.getLogger(EncryptionUtil.class);
    private static final String ALGORITHM = "SHA-256";
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int SALT_LENGTH = 16;

    public static String generateSalt() {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            secureRandom.nextBytes(salt);
            String generatedSalt = Base64.getEncoder().encodeToString(salt);
            logger.debug("Generated new salt successfully");
            return generatedSalt;
        } catch (Exception e) {
            logger.error("Error generating salt", e);
            throw new RuntimeException("Failed to generate salt", e);
        }
    }

    public static String hashPassword(String password, String salt) {
        if (password == null || password.isEmpty()) {
            logger.warn("Attempt to hash empty password");
            throw new IllegalArgumentException("Password cannot be empty");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(salt.getBytes());
            byte[] hashedPassword = digest.digest(password.getBytes());
            String result = Base64.getEncoder().encodeToString(hashedPassword);
            logger.debug("Password hashed successfully");
            return result;
        } catch (NoSuchAlgorithmException e) {
            String errorMsg = "Hashing algorithm not available: " + ALGORITHM;
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    public static boolean verifyPassword(String password, String hashedPassword, String salt) {
        if (password == null || password.isEmpty()) {
            logger.warn("Attempt to verify empty password");
            return false;
        }

        try {
            String newHashedPassword = hashPassword(password, salt);
            boolean matches = newHashedPassword.equals(hashedPassword);
            logger.debug("Password verification {}", matches ? "successful" : "failed");
            return matches;
        } catch (Exception e) {
            logger.error("Error verifying password", e);
            return false;
        }
    }
}