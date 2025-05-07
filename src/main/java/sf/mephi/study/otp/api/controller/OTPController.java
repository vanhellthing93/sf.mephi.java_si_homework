package sf.mephi.study.otp.api.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sf.mephi.study.otp.model.OTPCode;
import sf.mephi.study.otp.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sf.mephi.study.otp.util.JwtUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class OTPController {

    private static final Logger logger = LoggerFactory.getLogger(OTPController.class);
    private final OTPService otpService;
    private final SmsNotificationService smsNotificationService;
    private final TelegramNotificationService telegramNotificationService;
    private final EmailNotificationService emailNotificationService;
    private final FileNotificationService fileNotificationService;
    private final JwtUtil jwtUtil;

    public OTPController(OTPService otpService,
                         SmsNotificationService smsNotificationService,
                         TelegramNotificationService telegramNotificationService,
                         EmailNotificationService emailNotificationService,
                         FileNotificationService fileNotificationService,
                         JwtUtil jwtUtil) {
        this.otpService = otpService;
        this.smsNotificationService = smsNotificationService;
        this.telegramNotificationService = telegramNotificationService;
        this.emailNotificationService = emailNotificationService;
        this.fileNotificationService = fileNotificationService;
        this.jwtUtil = jwtUtil;
    }

    public HttpHandler sendCodeHandler() {
        return exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                String operationId = getQueryParam(exchange, "operationId");
                String toPhoneNumber = getQueryParam(exchange, "phone");
                String toEmail = getQueryParam(exchange, "email");

                if (operationId != null && toPhoneNumber != null) {
                    try {
                        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                        String token = authHeader.substring(7);
                        String username = jwtUtil.extractUsername(token);
                        OTPCode otpCode = otpService.generateOTP(operationId, username);
                        smsNotificationService.sendCode(toPhoneNumber, otpCode.getCode());
                        telegramNotificationService.sendCode(toPhoneNumber, otpCode.getCode());
                        emailNotificationService.sendCode(toEmail, otpCode.getCode());
                        fileNotificationService.saveCode(toPhoneNumber, otpCode.getCode());
                        sendResponse(exchange, 200, "OTP code sent successfully");
                    } catch (Exception e) {
                        logger.error("Failed to send OTP code for operationId: {}", operationId, e);
                        sendResponse(exchange, 500, "Failed to send OTP code");
                    }
                } else {
                    logger.warn("Invalid request parameters for sendCode: operationId={}, phone={}", operationId, toPhoneNumber);
                    sendResponse(exchange, 400, "Invalid request parameters");
                }
            } else {
                logger.warn("Method Not Allowed for sendCode");
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        };
    }

    public HttpHandler validateCodeHandler() {
        return exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                String operationId = getQueryParam(exchange, "operationId");
                String code = getQueryParam(exchange, "code");

                if (operationId != null && code != null) {
                    boolean isValid = otpService.validateOTP(operationId, code);
                    if (isValid) {
                        logger.info("OTP code has been verified for operationId: {}", operationId);
                        sendResponse(exchange, 200, "OTP code is valid");
                    } else {
                        logger.warn("Invalid OTP code for operationId: {}", operationId);
                        sendResponse(exchange, 400, "Invalid OTP code");
                    }
                } else {
                    logger.warn("Invalid request parameters for validateCode: operationId={}, code={}", operationId, code);
                    sendResponse(exchange, 400, "Invalid request parameters");
                }
            } else {
                logger.warn("Method Not Allowed for validateCode");
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        };
    }

    private String getQueryParam(HttpExchange exchange, String param) {
        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2 && keyValue[0].equals(param)) {
                    return keyValue[1];
                }
            }
        }
        return null;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
        logger.debug("Response sent with status code: {}", statusCode);
    }

    public void expireOTPs() {
        otpService.expireOTPs();
        logger.info("Existing OTP codes has been checked and renewed");
    }
}
