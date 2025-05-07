package sf.mephi.study.otp.service;

import sf.mephi.study.otp.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class FileNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(FileNotificationService.class);
    private final String filePath;

    public FileNotificationService() {
        this.filePath = AppConfig.getOtpFilePath();
        logger.debug("FileNotificationService initialized with file path: {}", filePath);
    }

    public void saveCode(String destination, String code) {
        String message = String.format("%s, your confirmation code is: %s", destination, code);
        writeToFile(message);
    }

    private void writeToFile(String message) {
        try (FileWriter writer = new FileWriter(Paths.get(filePath).toFile(), true)) {
            writer.write(message + System.lineSeparator());
            logger.debug("OTP code saved successfully to file: {}", filePath);
        } catch (IOException e) {
            logger.error("Error saving OTP code to file: {}", filePath, e);
        }
    }
}
