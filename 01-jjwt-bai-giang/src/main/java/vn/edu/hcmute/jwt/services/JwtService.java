package vn.edu.hcmute.jwt.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import vn.edu.hcmute.jwt.exception.InvalidJwtException;
import vn.edu.hcmute.jwt.exception.JwtExpiredException;
import vn.edu.hcmute.jwt.exception.JwtSignatureException;

/** Buoc 4 cua bai giang: tao va xac minh JWT bang thu vien JJWT. */
@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        long now = System.currentTimeMillis();
        try {
            return Jwts.builder()
                    .claims(extraClaims)
                    .subject(userDetails.getUsername())
                    .issuedAt(new Date(now))
                    .expiration(new Date(now + jwtExpiration))
                    .signWith(getSignInKey(), Jwts.SIG.HS256)
                    .compact();
        } catch (io.jsonwebtoken.security.WeakKeyException e) {
            throw new IllegalStateException("Khong the ky JWT (secret-key phai >= 256 bit)", e);
        }
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        Claims claims = extractAllClaims(token);
        return userDetails.getUsername().equals(claims.getSubject())
                && claims.getExpiration().after(new Date());
    }

    private Claims extractAllClaims(String token) {
        try {
            var parsed = Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token);
            if (!"HS256".equals(parsed.getHeader().getAlgorithm())) {
                throw new JwtSignatureException("Thuat toan ky khong duoc chap nhan");
            }
            Claims claims = parsed.getPayload();
            if (claims.getSubject() == null || claims.getExpiration() == null) {
                throw new InvalidJwtException("JWT thieu claim sub hoac exp");
            }
            return claims;
        } catch (ExpiredJwtException e) {
            throw new JwtExpiredException("JWT da het han");
        } catch (SignatureException e) {
            throw new JwtSignatureException("Chu ky JWT khong hop le");
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidJwtException("JWT sai dinh dang hoac khong hop le", e);
        }
    }

    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
    }
}
