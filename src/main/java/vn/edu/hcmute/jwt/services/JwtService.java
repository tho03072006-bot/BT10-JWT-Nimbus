package vn.edu.hcmute.jwt.services;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import vn.edu.hcmute.jwt.exception.InvalidJwtException;
import vn.edu.hcmute.jwt.exception.JwtExpiredException;
import vn.edu.hcmute.jwt.exception.JwtSignatureException;

/**
 * Buoc 4: JwtService, viet lai bang Nimbus JOSE + JWT.
 * Giu nguyen ten ham nhu bai giang, chi doi phan goi thu vien:
 *   Jwts.builder()...signWith(key, HS256).compact()   ->  JWTClaimsSet + SignedJWT + MACSigner
 *   Jwts.parser().verifyWith(key).parseSignedClaims() ->  SignedJWT.parse + MACVerifier
 */
@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, JWTClaimsSet::getSubject);
    }

    public <T> T extractClaim(String token, Function<JWTClaimsSet, T> claimsResolver) {
        final JWTClaimsSet claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        long now = System.currentTimeMillis();
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder();
        extraClaims.forEach(claims::claim);
        claims.subject(userDetails.getUsername())
                .issueTime(new Date(now))
                .expirationTime(new Date(now + expiration));

        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims.build());
        try {
            jwt.sign(new MACSigner(getSignInKey()));
        } catch (JOSEException e) {
            throw new IllegalStateException("Khong the ky JWT (secret-key phai >= 256 bit)", e);
        }
        return jwt.serialize();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, JWTClaimsSet::getExpirationTime);
    }

    /**
     * Parse, kiem tra thuat toan va chu ky, roi kiem tra het han.
     * Nem JwtSignatureException / JwtExpiredException / InvalidJwtException (deu la 401).
     */
    private JWTClaimsSet extractAllClaims(String token) {
        final SignedJWT jwt;
        try {
            jwt = SignedJWT.parse(token);
        } catch (ParseException e) {
            throw new InvalidJwtException("JWT sai dinh dang", e);
        }
        // Chi chap nhan HS256: chan "alg":"none" va viec ke tan cong tu chon thuat toan.
        if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm())) {
            throw new JwtSignatureException("Thuat toan ky khong duoc chap nhan");
        }
        try {
            if (!jwt.verify(new MACVerifier(getSignInKey()))) {
                throw new JwtSignatureException("Chu ky JWT khong hop le");
            }
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            if (claims.getSubject() == null || claims.getExpirationTime() == null) {
                throw new InvalidJwtException("JWT thieu claim sub hoac exp");
            }
            if (claims.getExpirationTime().before(new Date())) {
                throw new JwtExpiredException("JWT da het han");
            }
            return claims;
        } catch (JOSEException | ParseException e) {
            throw new InvalidJwtException("Khong the xac thuc JWT", e);
        }
    }

    /** Secret trong application.properties duoc giai ma Base64 thanh khoa HMAC (giong Decoders.BASE64 cua jjwt). */
    private byte[] getSignInKey() {
        return Base64.getDecoder().decode(secretKey);
    }
}
