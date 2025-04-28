package sf.mephi.study.otp.api.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sf.mephi.study.otp.model.User;
import sf.mephi.study.otp.service.UserService;
import sf.mephi.study.otp.util.EncryptionUtil;
import sf.mephi.study.otp.util.JwtUtil;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    public HttpHandler registerUserHandler() {
        return exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Парсинг данных из запроса
                String login = getQueryParam(exchange, "login");
                String password = getQueryParam(exchange, "password");
                String role = getQueryParam(exchange, "role");

                if (login != null && password != null && role != null) {
                    User.Role userRole = User.Role.valueOf(role.toUpperCase());
                    userService.registerUser(login, password, userRole);
                    sendResponse(exchange, 200, "User registered successfully");
                } else {
                    sendResponse(exchange, 400, "Invalid request parameters");
                }
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        };
    }

    public HttpHandler loginHandler() {
        return exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                String login = getQueryParam(exchange, "login");
                String password = getQueryParam(exchange, "password");

                if (login != null && password != null) {
                    Optional<User> userOptional = userService.getUserByLogin(login);
                    if (userOptional.isPresent()) {
                        User user = userOptional.get();
                        String hashedPassword = EncryptionUtil.hashPassword(password, user.getSalt());
                        if (user.getEncryptedPassword().equals(hashedPassword)) {
                            String token = jwtUtil.generateToken(login);
                            sendResponse(exchange, 200, "{\"token\":\"" + token + "\"}");
                        } else {
                            sendResponse(exchange, 401, "Invalid login or password");
                        }
                    } else {
                        sendResponse(exchange, 401, "Invalid login or password");
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
