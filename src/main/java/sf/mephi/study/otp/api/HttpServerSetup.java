package sf.mephi.study.otp.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sf.mephi.study.otp.api.controller.AdminController;
import sf.mephi.study.otp.api.controller.OTPController;
import sf.mephi.study.otp.api.controller.UserController;
import sf.mephi.study.otp.api.filter.JwtFilter;
import sf.mephi.study.otp.config.AppConfig;
import sf.mephi.study.otp.dao.OTPCodesDAO;
import sf.mephi.study.otp.dao.OTPConfigDAO;
import sf.mephi.study.otp.dao.UserDAO;
import sf.mephi.study.otp.service.*;
import sf.mephi.study.otp.util.JwtUtil;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HttpServerSetup {

    private static final Logger logger = LoggerFactory.getLogger(HttpServerSetup.class);

    public static void main(String[] args) {
        try {
            // Создаем DAO, сервисы и контроллеры
            UserDAO userDAO = new UserDAO();
            OTPCodesDAO otpCodesDAO = new OTPCodesDAO();
            OTPConfigDAO otpConfigDAO = new OTPConfigDAO();
            UserService userService = new UserService(userDAO);
            AdminService adminService = new AdminService(userDAO, otpConfigDAO);
            JwtUtil jwtUtil = new JwtUtil();
            UserController userController = new UserController(userService, jwtUtil);
            AdminController adminController = new AdminController(adminService, jwtUtil);

            // Создаем сервисы для отправки уведомлений
            SmsNotificationService smsNotificationService = new SmsNotificationService();
            TelegramNotificationService telegramNotificationService = new TelegramNotificationService();
            EmailNotificationService emailNotificationService = new EmailNotificationService();
            FileNotificationService fileNotificationService = new FileNotificationService();

            // Создаем контроллер для отправки OTP-кодов
            OTPService otpService = new OTPService(otpCodesDAO, otpConfigDAO);
            OTPController otpController = new OTPController(
                    otpService,
                    smsNotificationService,
                    telegramNotificationService,
                    emailNotificationService,
                    fileNotificationService,
                    jwtUtil
            );

            // Создаем HTTP-сервер
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            logger.debug("HTTP server created on port 8080");

            // Создаем фильтр JWT
            JwtFilter jwtFilter = new JwtFilter(jwtUtil);

            // Настраиваем обработчики для различных эндпоинтов
            server.createContext("/register", userController.registerUserHandler());
            logger.debug("Register endpoint configured");
            server.createContext("/login", userController.loginHandler());
            logger.debug("Login endpoint configured");

            // Применяем фильтр JWT к защищенным эндпоинтам
            server.createContext("/getUser", adminController.getUserHandler()).getFilters().add(jwtFilter);
            logger.debug("GetUser endpoint configured with JWT filter");
            server.createContext("/deleteUser", adminController.deleteUserHandler()).getFilters().add(jwtFilter);
            logger.debug("DeleteUser endpoint configured with JWT filter");
            server.createContext("/getAllUsers", adminController.getAllUsersHandler()).getFilters().add(jwtFilter);
            logger.debug("GetAllUsers endpoint configured with JWT filter");
            server.createContext("/updateOTPConfig", adminController.updateOTPConfigHandler()).getFilters().add(jwtFilter);
            logger.debug("UpdateOTPConfig endpoint configured with JWT filter");
            server.createContext("/getOTPConfig", adminController.getOTPConfigHandler()).getFilters().add(jwtFilter);
            logger.debug("GetOTPConfig endpoint configured with JWT filter");
            server.createContext("/sendOTP", otpController.sendCodeHandler()).getFilters().add(jwtFilter);
            logger.debug("SendOTP endpoint configured with JWT filter");
            server.createContext("/validateOTP", otpController.validateCodeHandler()).getFilters().add(jwtFilter);
            logger.debug("ValidateOTP endpoint configured with JWT filter");

            // Настраиваем периодическое обновление статуса истекших OTP-кодов
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(otpController::expireOTPs, 0, AppConfig.getOtpExpirationTime(), TimeUnit.SECONDS);
            logger.info("Scheduled task for expiring OTPs configured");

            // Запускаем сервер
            server.setExecutor(null); // creates a default executor
            server.start();
            logger.info("Server started on port 8080");
        } catch (IOException e) {
            logger.error("Failed to start server", e);
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
        logger.info("Response sent with status code: {}", statusCode);
    }
}
