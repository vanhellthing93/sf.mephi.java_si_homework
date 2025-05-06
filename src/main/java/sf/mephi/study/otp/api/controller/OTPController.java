package sf.mephi.study.otp.api.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sf.mephi.study.otp.model.OTPCode;
import sf.mephi.study.otp.service.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class OTPController {

    private final OTPService otpService;
    private final SmsNotificationService smsNotificationService;
    private final TelegramNotificationService telegramNotificationService;
    private final EmailNotificationService emailNotificationService;
    private final FileNotificationService fileNotificationService;


    public OTPController(OTPService otpService,
                         SmsNotificationService smsNotificationService,
                         TelegramNotificationService telegramNotificationService,
                         EmailNotificationService emailNotificationService,
                         FileNotificationService fileNotificationService) {        this.otpService = otpService;
        this.smsNotificationService = smsNotificationService;
        this.telegramNotificationService = telegramNotificationService;
        this.emailNotificationService = emailNotificationService;
        this.fileNotificationService = fileNotificationService;
    }

    public HttpHandler sendCodeHandler() {
        return exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                String operationId = getQueryParam(exchange, "operationId");
                String toPhoneNumber = getQueryParam(exchange, "phone");
                String toEmail = getQueryParam(exchange, "email");

                if (operationId != null && toPhoneNumber != null) {
                    try {
                        OTPCode otpCode = otpService.generateOTP(operationId);
                        smsNotificationService.sendCode(toPhoneNumber, otpCode.getCode());
                        telegramNotificationService.sendCode(toPhoneNumber, otpCode.getCode());
                        emailNotificationService.sendCode(toEmail, otpCode.getCode());
                        fileNotificationService.saveCode(toPhoneNumber, otpCode.getCode());
                        sendResponse(exchange, 200, "OTP code sent successfully");
                    } catch (Exception e) {
                        sendResponse(exchange, 500, "Failed to send OTP code");
                    }
                } else {
                    sendResponse(exchange, 400, "Invalid request parameters");
                }
            } else {
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
                        sendResponse(exchange, 200, "OTP code is valid");
                    } else {
                        sendResponse(exchange, 400, "Invalid OTP code");
                    }
                } else {
                    sendResponse(exchange, 400, "Invalid request parameters");
                }
            } else {
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
    }
}
