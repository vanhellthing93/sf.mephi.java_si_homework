package sf.mephi.study.otp.service;

import sf.mephi.study.otp.dao.OTPCodesDAO;
import sf.mephi.study.otp.dao.OTPConfigDAO;
import sf.mephi.study.otp.model.OTPCode;
import sf.mephi.study.otp.model.OTPConfig;
import sf.mephi.study.otp.util.OTPGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class OTPService {

    private final OTPCodesDAO otpCodesDAO;
    private final OTPConfigDAO otpConfigDAO;

    public OTPService(OTPCodesDAO otpCodesDAO, OTPConfigDAO otpConfigDAO) {
        this.otpCodesDAO = otpCodesDAO;
        this.otpConfigDAO = otpConfigDAO;
    }

    public OTPCode generateOTP(String operationId) {
        Optional<OTPConfig> configOptional = otpConfigDAO.getConfig();
        if (configOptional.isPresent()) {
            OTPConfig config = configOptional.get();
            String code = OTPGenerator.generateOTP(config.getCodeLength());
            OTPCode otpCode = new OTPCode(0, operationId, code, OTPCode.Status.ACTIVE, LocalDateTime.now());
            // проверяем есть ли в бд коды с такой же operation id и помечаем их expired
            Optional<OTPCode> otpCodeOptional = otpCodesDAO.findActiveByOperationId(operationId);
            if (otpCodeOptional.isPresent()) {
                OTPCode oldOtpCode = otpCodeOptional.get();
                expireOTPById(oldOtpCode.getId());
            }
            otpCodesDAO.save(otpCode);
            return otpCode;
        } else {
            throw new IllegalStateException("OTP configuration is not set");
        }
    }

    public List<OTPCode> getOTPsByOperationId(String operationId) {
        return otpCodesDAO.findAllByOperationId(operationId);
    }

    public List<OTPCode> getAllOTPs() {
        return otpCodesDAO.findAll();
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
                    return true;
                } else {
                    otpCodesDAO.updateStatus(otpCode.getId(), OTPCode.Status.EXPIRED);
                }
            }
        }
        return false;
    }

    public void updateOTPConfig(int codeLength, int expirationTime) {
        OTPConfig config = new OTPConfig(0, codeLength, expirationTime);
        otpConfigDAO.saveOrUpdateConfig(config);
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
                }
            }
        }
    }

    public void expireOTPById(int otpId) {
        otpCodesDAO.updateStatus(otpId, OTPCode.Status.EXPIRED);
    }

}