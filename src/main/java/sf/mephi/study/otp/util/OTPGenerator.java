package sf.mephi.study.otp.util;

import java.security.SecureRandom;

public class OTPGenerator {

    private static final String CHARACTERS = "0123456789";
    private static final SecureRandom random = new SecureRandom();

    /**
     * Генерирует случайный OTP-код заданной длины.
     *
     * @param length длина OTP-кода
     * @return сгенерированный OTP-код
     */
    public static String generateOTP(int length) {
        StringBuilder otp = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            otp.append(CHARACTERS.charAt(index));
        }
        return otp.toString();
    }
}
