package sf.mephi.study.otp.api.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sf.mephi.study.otp.model.User;
import sf.mephi.study.otp.service.UserService;
import sf.mephi.study.otp.util.EncryptionUtil;
import sf.mephi.study.otp.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
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
                    try {
                        //проверка, что пользователь с таким username ещё не создан
                        Optional<User> userOptional = userService.getUserByLogin(login);
                        if (userOptional.isPresent()) {
                            logger.warn("Attempt to register user with username that is already used");
                            sendResponse(exchange, 400, "User with this login already exist");
                            return;
                        }
                        //проверка, что пользователь с ролью ADMIN ещё не создан
                        User.Role userRole = User.Role.valueOf(role.toUpperCase());
                        if (userRole == User.Role.ADMIN) {
                            Optional<User> adminUserOptional = userService.getUserByRole("ADMIN");
                            if (adminUserOptional.isPresent()) {
                                logger.warn("Attempt to register another user with role ADMIN");
                                sendResponse(exchange, 400, "User with role ADMIN already exist");
                                return;
                            }
                        }
                        userService.registerUser(login, password, userRole);
                        logger.info("User registered successfully: {}", login);
                        sendResponse(exchange, 200, "User registered successfully");
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid role specified for user registration: {}", role);
                        sendResponse(exchange, 400, "Invalid role specified");
                    }
                } else {
                    logger.warn("Invalid request parameters for registerUser: login={}, password={}, role={}", login, password, role);
                    sendResponse(exchange, 400, "Invalid request parameters");
                }
            } else {
                logger.warn("Method Not Allowed for registerUser");
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
                            logger.info("User logged in successfully: {}", login);
                            sendResponse(exchange, 200, "{\"token\":\"" + token + "\"}");
                        } else {
                            logger.warn("Invalid login or password for user: {}", login);
                            sendResponse(exchange, 401, "Invalid login or password");
                        }
                    } else {
                        logger.warn("Invalid login or password for user: {}", login);
                        sendResponse(exchange, 401, "Invalid login or password");
                    }
                } else {
                    logger.warn("Invalid request parameters for login: login={}, password={}", login, password);
                    sendResponse(exchange, 400, "Invalid request parameters");
                }
            } else {
                logger.warn("Method Not Allowed for login");
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
}
