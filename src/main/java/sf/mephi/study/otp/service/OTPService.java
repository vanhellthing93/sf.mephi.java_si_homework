package sf.mephi.study.otp.service;

import sf.mephi.study.otp.dao.OTPCodesDAO;
import sf.mephi.study.otp.dao.OTPConfigDAO;
import sf.mephi.study.otp.model.OTPCode;
import sf.mephi.study.otp.model.OTPConfig;
import sf.mephi.study.otp.util.OTPGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class OTPService {

    private static final Logger logger = LoggerFactory.getLogger(OTPService.class);
    private final OTPCodesDAO otpCodesDAO;
    private final OTPConfigDAO otpConfigDAO;

    public OTPService(OTPCodesDAO otpCodesDAO, OTPConfigDAO otpConfigDAO) {
        this.otpCodesDAO = otpCodesDAO;
        this.otpConfigDAO = otpConfigDAO;
    }

    public OTPCode generateOTP(String operationId, String username) {
        Optional<OTPConfig> configOptional = otpConfigDAO.getConfig();
        if (configOptional.isPresent()) {
            OTPConfig config = configOptional.get();
            String code = OTPGenerator.generateOTP(config.getCodeLength());
            OTPCode otpCode = new OTPCode(0, operationId, code, OTPCode.Status.ACTIVE, LocalDateTime.now(), username);

            // Проверяем, есть ли в базе данных коды с такой же operationId и помечаем их как expired
            Optional<OTPCode> otpCodeOptional = otpCodesDAO.findActiveByOperationId(operationId);
            if (otpCodeOptional.isPresent()) {
                OTPCode oldOtpCode = otpCodeOptional.get();
                expireOTPById(oldOtpCode.getId());
            }

            otpCodesDAO.save(otpCode);
            logger.debug("OTP code generated for operationId: {}", operationId);
            return otpCode;
        } else {
            logger.error("OTP configuration is not set");
            throw new IllegalStateException("OTP configuration is not set");
        }
    }

    public boolean validateOTP(String operationId, String code) {
        Optional<OTPCode> otpCodeOptional = otpCodesDAO.findActiveByOperationId(operationId);
        if (otpCodeOptional.isPresent()) {
            OTPCode otpCode = otpCodeOptional.get();
            if (otpCode.getCode().equals(code) && otpCode.getStatus() == OTPCode.Status.ACTIVE) {
                // Проверяем, не истекло ли время действия кода
                OTPConfig config = otpConfigDAO.getConfig().orElseThrow(() -> new IllegalStateException("OTP configuration is not set"));
                LocalDateTime expirationTime = otpCode.getCreatedAt().plusSeconds(config.getExpirationTime());
                if (LocalDateTime.now().isBefore(expirationTime)) {
                    otpCodesDAO.updateStatus(otpCode.getId(), OTPCode.Status.USED);
                    logger.debug("OTP code validated successfully for operationId: {}", operationId);
                    return true;
                } else {
                    otpCodesDAO.updateStatus(otpCode.getId(), OTPCode.Status.EXPIRED);
                    logger.debug("OTP code expired for operationId: {}", operationId);
                }
            }
        }
        logger.debug("OTP validation failed for operationId: {}", operationId);
        return false;
    }

    public void expireOTPs() {
        List<OTPCode> otpCodes = otpCodesDAO.findAll();
        OTPConfig config = otpConfigDAO.getConfig().orElseThrow(() -> new IllegalStateException("OTP configuration is not set"));
        LocalDateTime now = LocalDateTime.now();
        for (OTPCode otpCode : otpCodes) {
            if (otpCode.getStatus() == OTPCode.Status.ACTIVE) {
                LocalDateTime expirationTime = otpCode.getCreatedAt().plusSeconds(config.getExpirationTime());
                if (now.isAfter(expirationTime)) {
                    otpCodesDAO.updateStatus(otpCode.getId(), OTPCode.Status.EXPIRED);
                    logger.debug("OTP code expired for operationId: {}", otpCode.getOperationId());
                }
            }
        }
    }

    public void expireOTPById(int otpId) {
        otpCodesDAO.updateStatus(otpId, OTPCode.Status.EXPIRED);
        logger.debug("OTP code expired for otpId: {}", otpId);
    }
}
