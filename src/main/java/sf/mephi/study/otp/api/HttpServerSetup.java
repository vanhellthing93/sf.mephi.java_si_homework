package sf.mephi.study.otp.api;

import sf.mephi.study.otp.api.controller.AdminController;
import sf.mephi.study.otp.api.controller.OTPController;
import sf.mephi.study.otp.api.controller.UserController;
import sf.mephi.study.otp.api.filter.JwtFilter;
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

public class HttpServerSetup {

    public static void main(String[] args) throws IOException {
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
                fileNotificationService
        );


        // Создаем HTTP-сервер
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Создаем фильтр JWT
        JwtFilter jwtFilter = new JwtFilter(jwtUtil);

        // Настраиваем обработчики для различных эндпоинтов
        server.createContext("/register", userController.registerUserHandler());
        server.createContext("/login", userController.loginHandler());

        // Применяем фильтр JWT к защищенным эндпоинтам
        server.createContext("/getUser", adminController.getUserHandler()).getFilters().add(jwtFilter);
        server.createContext("/deleteUser", adminController.deleteUserHandler()).getFilters().add(jwtFilter);
        server.createContext("/getAllUsers", adminController.getAllUsersHandler()).getFilters().add(jwtFilter);
        server.createContext("/updateOTPConfig", adminController.updateOTPConfigHandler()).getFilters().add(jwtFilter);
        server.createContext("/getOTPConfig", adminController.getOTPConfigHandler()).getFilters().add(jwtFilter);
        server.createContext("/sendOTP", otpController.sendCodeHandler()); // Добавляем эндпоинт для отправки OTP-кодов
        server.createContext("/validateOTP", otpController.validateCodeHandler()); // Добавляем эндпоинт для валидации OTP-кодов

        server.createContext("/secure", exchange -> {
            String username = (String) exchange.getAttribute("username");
            if (username != null) {
                sendResponse(exchange, 200, "Secure content for " + username);
            } else {
                sendResponse(exchange, 403, "Forbidden");
            }
        }).getFilters().add(jwtFilter);

        // Запускаем сервер
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started on port 8080");
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
}