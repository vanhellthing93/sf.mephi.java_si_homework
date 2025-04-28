package sf.mephi.study.otp.api.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sf.mephi.study.otp.model.OTPConfig;
import sf.mephi.study.otp.model.User;
import sf.mephi.study.otp.service.AdminService;
import sf.mephi.study.otp.util.JwtUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class AdminController {

    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    public AdminController(AdminService adminService, JwtUtil jwtUtil) {
        this.adminService = adminService;
        this.jwtUtil = jwtUtil;
    }


    public HttpHandler getUserHandler() {
        return exchange -> {
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
                                    sendResponse(exchange, 200, user.get().toString());
                                } else {
                                    sendResponse(exchange, 404, "User not found");
                                }
                            } else {
                                sendResponse(exchange, 400, "Invalid request parameters");
                            }
                        } else {
                            sendResponse(exchange, 403, "Forbidden: Admin role required");
                        }
                    } else {
                        sendResponse(exchange, 403, "Forbidden");
                    }
                } else {
                    sendResponse(exchange, 401, "Unauthorized");
                }
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        };
    }

    public HttpHandler deleteUserHandler() {
        return exchange -> {
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
                                sendResponse(exchange, 200, "User deleted successfully");
                            } else {
                                sendResponse(exchange, 400, "Invalid request parameters");
                            }
                        } else {
                            sendResponse(exchange, 403, "Forbidden: Admin role required");
                        }
                    } else {
                        sendResponse(exchange, 403, "Forbidden");
                    }
                } else {
                    sendResponse(exchange, 401, "Unauthorized");
                }
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        };
    }

    public HttpHandler getAllUsersHandler() {
        return exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    String username = jwtUtil.extractUsername(token);
                    if (jwtUtil.validateToken(token, username)) {
                        Optional<User> adminUser = adminService.getUserByLogin(username);
                        if (adminUser.isPresent() && adminUser.get().getRole() == User.Role.ADMIN) {
                            List<User> users = adminService.getAllUsers();
                            sendResponse(exchange, 200, users.toString());
                        } else {
                            sendResponse(exchange, 403, "Forbidden: Admin role required");
                        }
                    } else {
                        sendResponse(exchange, 403, "Forbidden");
                    }
                } else {
                    sendResponse(exchange, 401, "Unauthorized");
                }
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        };
    }

    public HttpHandler updateOTPConfigHandler() {
        return exchange -> {
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
                                sendResponse(exchange, 200, "OTP config updated successfully");
                            } else {
                                sendResponse(exchange, 400, "Invalid request parameters");
                            }
                        } else {
                            sendResponse(exchange, 403, "Forbidden: Admin role required");
                        }
                    } else {
                        sendResponse(exchange, 403, "Forbidden");
                    }
                } else {
                    sendResponse(exchange, 401, "Unauthorized");
                }
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        };
    }

    public HttpHandler getOTPConfigHandler() {
        return exchange -> {
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
                                sendResponse(exchange, 200, otpConfig.get().toString());
                            } else {
                                sendResponse(exchange, 404, "OTP config not found");
                            }
                        } else {
                            sendResponse(exchange, 403, "Forbidden: Admin role required");
                        }
                    } else {
                        sendResponse(exchange, 403, "Forbidden");
                    }
                } else {
                    sendResponse(exchange, 401, "Unauthorized");
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
