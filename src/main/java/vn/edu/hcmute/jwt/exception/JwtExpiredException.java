package vn.edu.hcmute.jwt.exception;

/** Tuong duong ExpiredJwtException cua jjwt: token da qua han (claim exp). */
public class JwtExpiredException extends InvalidJwtException {

    public JwtExpiredException(String message) {
        super(message);
    }
}
