package sf.mephi.study.otp.api.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sf.mephi.study.otp.model.User;
import sf.mephi.study.otp.service.UserService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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

    public HttpHandler getUserHandler() {
        return exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                String login = getQueryParam(exchange, "login");
                if (login != null) {
                    Optional<User> user = userService.getUserByLogin(login);
                    if (user.isPresent()) {
                        sendResponse(exchange, 200, user.get().toString());
                    } else {
                        sendResponse(exchange, 404, "User not found");
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
