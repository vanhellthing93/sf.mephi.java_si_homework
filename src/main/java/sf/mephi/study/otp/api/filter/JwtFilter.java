package sf.mephi.study.otp.api.filter;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import sf.mephi.study.otp.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class JwtFilter extends Filter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            if (username != null && jwtUtil.validateToken(token, username)) {
                logger.debug("Token validated successfully for user: {}", username);
                exchange.setAttribute("username", username);
                chain.doFilter(exchange);
            } else {
                logger.warn("Forbidden: Invalid JWT token");
                sendResponse(exchange, 403, "Forbidden: Invalid token");
            }
        } else {
            logger.warn("Unauthorized: Missing or invalid token");
            sendResponse(exchange, 401, "Unauthorized: Missing or invalid token");
        }
    }

    @Override
    public String description() {
        return "JWT Filter";
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
        logger.debug("Response sent with status code: {}", statusCode);
    }
}
