package vn.edu.hcmute.jwt.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import vn.edu.hcmute.jwt.entity.User;
import vn.edu.hcmute.jwt.exception.InvalidJwtException;
import vn.edu.hcmute.jwt.exception.JwtExpiredException;
import vn.edu.hcmute.jwt.exception.JwtSignatureException;

class JwtServiceTest {

    /** 64 ky tu hex, giai ma Base64 thanh 48 byte (384 bit) giong cach bai giang dung secret. */
    private static final String SECRET = "3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b";
    private static final String OTHER_SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    private static JwtService service(String secret, long expirationMillis) {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secretKey", secret);
        ReflectionTestUtils.setField(service, "jwtExpiration", expirationMillis);
        return service;
    }

    private static UserDetails user(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("x");
        return user;
    }

    @Test
    void generatedTokenHasThreePartsHs256AndUserEmailAsSubject() throws Exception {
        String token = service(SECRET, 3_600_000).generateToken(user("trungnh@hcmute.edu.vn"));

        SignedJWT parsed = SignedJWT.parse(token);
        assertThat(token.split("\\.")).hasSize(3);
        assertThat(parsed.getHeader().getAlgorithm()).isEqualTo(JWSAlgorithm.HS256);
        assertThat(parsed.getJWTClaimsSet().getSubject()).isEqualTo("trungnh@hcmute.edu.vn");
        long lifetime = parsed.getJWTClaimsSet().getExpirationTime().getTime()
                - parsed.getJWTClaimsSet().getIssueTime().getTime();
        assertThat(lifetime).isEqualTo(3_600_000L);
    }

    @Test
    void extractsUsernameAndExtraClaims() {
        JwtService service = service(SECRET, 3_600_000);

        String token = service.generateToken(Map.of("role", "STUDENT"), user("a@b.vn"));

        assertThat(service.extractUsername(token)).isEqualTo("a@b.vn");
        String role = service.extractClaim(token, c -> (String) c.getClaim("role"));
        assertThat(role).isEqualTo("STUDENT");
        assertThat(service.getExpirationTime()).isEqualTo(3_600_000L);
    }

    @Test
    void tokenIsValidOnlyForItsOwnUser() {
        JwtService service = service(SECRET, 3_600_000);
        String token = service.generateToken(user("a@b.vn"));

        assertThat(service.isTokenValid(token, user("a@b.vn"))).isTrue();
        assertThat(service.isTokenValid(token, user("khac@b.vn"))).isFalse();
    }

    @Test
    void rejectsTamperedPayloadAsSignatureError() {
        JwtService service = service(SECRET, 3_600_000);
        String[] parts = service.generateToken(user("a@b.vn")).split("\\.");
        String forgedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(
                new String(Base64.getUrlDecoder().decode(parts[1])).replace("a@b.vn", "z@b.vn").getBytes());

        assertThatThrownBy(() -> service.extractUsername(parts[0] + "." + forgedPayload + "." + parts[2]))
                .isInstanceOf(JwtSignatureException.class);
    }

    @Test
    void rejectsTokenSignedWithAnotherSecret() {
        String foreign = service(OTHER_SECRET, 3_600_000).generateToken(user("a@b.vn"));

        assertThatThrownBy(() -> service(SECRET, 3_600_000).extractUsername(foreign))
                .isInstanceOf(JwtSignatureException.class);
    }

    @Test
    void rejectsExpiredToken() {
        JwtService service = service(SECRET, -1000);
        String expired = service.generateToken(user("a@b.vn"));

        assertThatThrownBy(() -> service.extractUsername(expired)).isInstanceOf(JwtExpiredException.class);
    }

    @Test
    void rejectsUnsignedAlgNoneToken() {
        JWTClaimsSet claims = new JWTClaimsSet.Builder().subject("a@b.vn")
                .expirationTime(new Date(System.currentTimeMillis() + 60_000)).build();
        String none = new PlainJWT(claims).serialize();

        assertThatThrownBy(() -> service(SECRET, 3_600_000).extractUsername(none))
                .isInstanceOf(InvalidJwtException.class);
    }

    @Test
    void rejectsHs384EvenWithCorrectSecret() throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder().subject("a@b.vn")
                .expirationTime(new Date(System.currentTimeMillis() + 60_000)).build();
        SignedJWT hs512 = new SignedJWT(new JWSHeader(JWSAlgorithm.HS384), claims);
        hs512.sign(new MACSigner(Base64.getDecoder().decode(SECRET)));

        assertThatThrownBy(() -> service(SECRET, 3_600_000).extractUsername(hs512.serialize()))
                .isInstanceOf(JwtSignatureException.class);
    }

    @Test
    void rejectsGarbageToken() {
        assertThatThrownBy(() -> service(SECRET, 3_600_000).extractUsername("khong-phai-jwt"))
                .isInstanceOf(InvalidJwtException.class);
    }

    @Test
    void refusesToSignWithKeyShorterThan256Bits() {
        JwtService weak = service("YWJjZA==", 3_600_000); // "abcd" = 32 bit

        assertThatThrownBy(() -> weak.generateToken(user("a@b.vn"))).isInstanceOf(IllegalStateException.class);
    }
}
