package sf.mephi.study.otp.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sf.mephi.study.otp.config.AppConfig;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static final String SECRET_KEY = AppConfig.getJwtSecret();
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    private static final long EXPIRATION_TIME = AppConfig.getJwtExpirationTime();
    private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS256;

    public String extractUsername(String token) {
        try {
            String username = extractClaim(token, Claims::getSubject);
            logger.debug("Extracted username '{}' from JWT", username);
            return username;
        } catch (Exception e) {
            logger.debug("Failed to extract username from JWT: {}", e.getMessage());
            return null;
        }
    }

    public Date extractExpiration(String token) {
        try {
            Date expiration = extractClaim(token, Claims::getExpiration);
            logger.debug("Extracted JWT expiration: {}", expiration);
            return expiration;
        } catch (Exception e) {
            logger.debug("Failed to extract expiration from JWT: {}", e.getMessage());
            throw e;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            logger.debug("Failed to extract claim from JWT", e);
            throw e;
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SIGNING_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.debug("Expired JWT token: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            logger.debug("JWT signature validation failed", e);
            throw e;
        } catch (Exception e) {
            logger.debug("Invalid JWT token", e);
            throw e;
        }
    }

    private Boolean isTokenExpired(String token) {
        try {
            boolean expired = extractExpiration(token).before(new Date());
            if (expired) {
                logger.debug("Token is expired");
            }
            return expired;
        } catch (Exception e) {
            logger.debug("Error checking token expiration", e);
            return true;
        }
    }

    public String generateToken(String username) {
        if (username == null || username.isEmpty()) {
            logger.debug("Attempt to generate token for empty username");
            throw new IllegalArgumentException("Username cannot be empty");
        }

        Map<String, Object> claims = new HashMap<>();
        String token = createToken(claims, username);
        logger.debug("Generated new JWT token for user '{}'", username);
        return token;
    }

    private String createToken(Map<String, Object> claims, String subject) {
        try {
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .signWith(SIGNING_KEY, SIGNATURE_ALGORITHM)
                    .compact();
        } catch (Exception e) {
            logger.debug("Failed to create JWT token", e);
            throw e;
        }
    }

    public Boolean validateToken(String token, String username) {
        if (token == null || token.isEmpty()) {
            logger.debug("Empty token provided for validation");
            return false;
        }

        try {
            final String extractedUsername = extractUsername(token);
            boolean isValid = extractedUsername != null
                    && extractedUsername.equals(username)
                    && !isTokenExpired(token);

            if (!isValid) {
                logger.debug("Token validation failed for user '{}'", username);
            } else {
                logger.debug("Token validated successfully for user '{}'", username);
            }

            return isValid;
        } catch (Exception e) {
            logger.debug("Error validating token", e);
            return false;
        }
    }
}