package sf.mephi.study.otp.api.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sf.mephi.study.otp.model.OTPConfig;
import sf.mephi.study.otp.model.User;
import sf.mephi.study.otp.service.AdminService;
import sf.mephi.study.otp.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    public AdminController(AdminService adminService, JwtUtil jwtUtil) {
        this.adminService = adminService;
        this.jwtUtil = jwtUtil;
    }

    public HttpHandler deleteUserHandler() {
        return exchange -> {
            try {
                if ("DELETE".equals(exchange.getRequestMethod())) {
                    String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        String username = jwtUtil.extractUsername(token);
                        if (jwtUtil.validateToken(token, username)) {
                            Optional<User> adminUser = adminService.getUserByLogin(username);
                            if (adminUser.isPresent() && adminUser.get().getRole() == User.Role.ADMIN) {
                                String login = getQueryParam(exchange, "login");

                                if (login != null) {
                                    adminService.deleteUser(login);
                                    logger.info("User {} deleted successfully by admin {}", login, username);
                                    sendResponse(exchange, 200, "User deleted successfully");
                                } else {
                                    logger.warn("Invalid request parameters for deleteUser");
                                    sendResponse(exchange, 400, "Invalid request parameters");
                                }
                            } else {
                                logger.warn("Forbidden: Admin role required for deleteUser");
                                sendResponse(exchange, 403, "Forbidden: Admin role required");
                            }
                        } else {
                            logger.warn("Forbidden: Invalid token for deleteUser");
                            sendResponse(exchange, 403, "Forbidden: Invalid token");
                        }
                    } else {
                        logger.warn("Unauthorized: Missing or invalid token for deleteUser");
                        sendResponse(exchange, 401, "Unauthorized: Missing or invalid token");
                    }
                } else {
                    logger.warn("Method Not Allowed for deleteUser");
                    sendResponse(exchange, 405, "Method Not Allowed");
                }
            } catch (Exception e) {
                logger.error("Exception in deleteUserHandler", e);
                sendResponse(exchange, 500, "Internal Server Error");
            }
        };
    }

    public HttpHandler getAllUsersHandler() {
        return exchange -> {
            try {
                if ("GET".equals(exchange.getRequestMethod())) {
                    String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        String username = jwtUtil.extractUsername(token);
                        if (jwtUtil.validateToken(token, username)) {
                            Optional<User> adminUser = adminService.getUserByLogin(username);
                            if (adminUser.isPresent() && adminUser.get().getRole() == User.Role.ADMIN) {
                                List<User> users = adminService.getAllUsers();
                                logger.info("All users retrieved successfully by admin {}", username);
                                sendResponse(exchange, 200, users.toString());
                            } else {
                                logger.warn("Forbidden: Admin role required for getAllUsers");
                                sendResponse(exchange, 403, "Forbidden: Admin role required");
                            }
                        } else {
                            logger.warn("Forbidden: Invalid token for getAllUsers");
                            sendResponse(exchange, 403, "Forbidden: Invalid token");
                        }
                    } else {
                        logger.warn("Unauthorized: Missing or invalid token for getAllUsers");
                        sendResponse(exchange, 401, "Unauthorized: Missing or invalid token");
                    }
                } else {
                    logger.warn("Method Not Allowed for getAllUsers");
                    sendResponse(exchange, 405, "Method Not Allowed");
                }
            } catch (Exception e) {
                logger.error("Exception in getAllUsersHandler", e);
                sendResponse(exchange, 500, "Internal Server Error");
            }
        };
    }

    public HttpHandler updateOTPConfigHandler() {
        return exchange -> {
            try {
                if ("PUT".equals(exchange.getRequestMethod())) {
                    String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        String username = jwtUtil.extractUsername(token);
                        if (jwtUtil.validateToken(token, username)) {
                            Optional<User> adminUser = adminService.getUserByLogin(username);
                            if (adminUser.isPresent() && adminUser.get().getRole() == User.Role.ADMIN) {
                                String codeLengthStr = getQueryParam(exchange, "codeLength");
                                String expirationTimeStr = getQueryParam(exchange, "expirationTime");

                                if (codeLengthStr != null && expirationTimeStr != null) {
                                    int codeLength = Integer.parseInt(codeLengthStr);
                                    int expirationTime = Integer.parseInt(expirationTimeStr);
                                    adminService.updateOTPConfig(codeLength, expirationTime);
                                    logger.info("OTP config updated successfully by admin {}", username);
                                    sendResponse(exchange, 200, "OTP config updated successfully");
                                } else {
                                    logger.warn("Invalid request parameters for updateOTPConfig");
                                    sendResponse(exchange, 400, "Invalid request parameters");
                                }
                            } else {
                                logger.warn("Forbidden: Admin role required for updateOTPConfig");
                                sendResponse(exchange, 403, "Forbidden: Admin role required");
                            }
                        } else {
                            logger.warn("Forbidden: Invalid token for updateOTPConfig");
                            sendResponse(exchange, 403, "Forbidden: Invalid token");
                        }
                    } else {
                        logger.warn("Unauthorized: Missing or invalid token for updateOTPConfig");
                        sendResponse(exchange, 401, "Unauthorized: Missing or invalid token");
                    }
                } else {
                    logger.warn("Method Not Allowed for updateOTPConfig");
                    sendResponse(exchange, 405, "Method Not Allowed");
                }
            } catch (Exception e) {
                logger.error("Exception in updateOTPConfigHandler", e);
                sendResponse(exchange, 500, "Internal Server Error");
            }
        };
    }

    public HttpHandler getOTPConfigHandler() {
        return exchange -> {
            try {
                if ("GET".equals(exchange.getRequestMethod())) {
                    String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        String username = jwtUtil.extractUsername(token);
                        if (jwtUtil.validateToken(token, username)) {
                            Optional<User> adminUser = adminService.getUserByLogin(username);
                            if (adminUser.isPresent() && adminUser.get().getRole() == User.Role.ADMIN) {
                                Optional<OTPConfig> otpConfig = adminService.getOTPConfig();
                                if (otpConfig.isPresent()) {
                                    logger.info("OTP config retrieved successfully by admin {}", username);
                                    sendResponse(exchange, 200, otpConfig.get().toString());
                                } else {
                                    logger.warn("OTP config not found for getOTPConfig");
                                    sendResponse(exchange, 404, "OTP config not found");
                                }
                            } else {
                                logger.warn("Forbidden: Admin role required for getOTPConfig");
                                sendResponse(exchange, 403, "Forbidden: Admin role required");
                            }
                        } else {
                            logger.warn("Forbidden: Invalid token for getOTPConfig");
                            sendResponse(exchange, 403, "Forbidden: Invalid token");
                        }
                    } else {
                        logger.warn("Unauthorized: Missing or invalid token for getOTPConfig");
                        sendResponse(exchange, 401, "Unauthorized: Missing or invalid token");
                    }
                } else {
                    logger.warn("Method Not Allowed for getOTPConfig");
                    sendResponse(exchange, 405, "Method Not Allowed");
                }
            } catch (Exception e) {
                logger.error("Exception in getOTPConfigHandler", e);
                sendResponse(exchange, 500, "Internal Server Error");
            }
        };
    }

    public HttpHandler getUserHandler() {
        return exchange -> {
            try {
                if ("GET".equals(exchange.getRequestMethod())) {
                    String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        String username = jwtUtil.extractUsername(token);
                        if (jwtUtil.validateToken(token, username)) {
                            Optional<User> adminUser = adminService.getUserByLogin(username);
                            if (adminUser.isPresent() && adminUser.get().getRole() == User.Role.ADMIN) {
                                String login = getQueryParam(exchange, "login");
                                if (login != null) {
                                    Optional<User> user = adminService.getUserByLogin(login);
                                    if (user.isPresent()) {
                                        logger.info("User {} retrieved successfully by admin {}", login, username);
                                        sendResponse(exchange, 200, user.get().toString());
                                    } else {
                                        logger.warn("User not found for getUser: {}", login);
                                        sendResponse(exchange, 404, "User not found");
                                    }
                                } else {
                                    logger.warn("Invalid request parameters for getUser");
                                    sendResponse(exchange, 400, "Invalid request parameters");
                                }
                            } else {
                                logger.warn("Forbidden: Admin role required for getUser");
                                sendResponse(exchange, 403, "Forbidden: Admin role required");
                            }
                        } else {
                            logger.warn("Forbidden: Invalid token for getUser");
                            sendResponse(exchange, 403, "Forbidden: Invalid token");
                        }
                    } else {
                        logger.warn("Unauthorized: Missing or invalid token for getUser");
                        sendResponse(exchange, 401, "Unauthorized: Missing or invalid token");
                    }
                } else {
                    logger.warn("Method Not Allowed for getUser");
                    sendResponse(exchange, 405, "Method Not Allowed");
                }
            } catch (Exception e) {
                logger.error("Exception in getUserHandler", e);
                sendResponse(exchange, 500, "Internal Server Error");
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
        logger.info("Response sent with status code: {}", statusCode);
    }
}
