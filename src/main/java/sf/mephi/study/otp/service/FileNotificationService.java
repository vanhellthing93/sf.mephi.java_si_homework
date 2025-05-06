package sf.mephi.study.otp.service;

import sf.mephi.study.otp.config.AppConfig;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class FileNotificationService {

    private final String filePath;

    public FileNotificationService() {
        this.filePath = AppConfig.getOtpFilePath();
    }

    public void saveCode(String destination, String code) {
        String message = String.format("%s, your confirmation code is: %s", destination, code);
        writeToFile(message);
    }

    private void writeToFile(String message) {
        try (FileWriter writer = new FileWriter(Paths.get(filePath).toFile(), true)) {
            writer.write(message + System.lineSeparator());
            System.out.println("OTP code saved successfully");
        } catch (IOException e) {
            System.out.println("Error saving OTP code: " + e.getMessage());
        }
    }
}
