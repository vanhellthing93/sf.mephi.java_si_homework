package sf.mephi.study.otp.api.filter;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import sf.mephi.study.otp.util.JwtUtil;

import java.io.IOException;

public class JwtFilter extends Filter {

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
            if (jwtUtil.validateToken(token, username)) {
                exchange.setAttribute("username", username);
                chain.doFilter(exchange);
            } else {
                exchange.sendResponseHeaders(403, -1); // Forbidden
            }
        } else {
            exchange.sendResponseHeaders(401, -1); // Unauthorized
        }
    }

    @Override
    public String description() {
        return "JWT Filter";
    }
}