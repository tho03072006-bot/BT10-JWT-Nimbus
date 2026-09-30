package vn.edu.hcmute.jwt.exception;

/** JWT khong the su dung (sai dinh dang, sai chu ky hoac het han). Luon tuong ung HTTP 401. */
public class InvalidJwtException extends RuntimeException {

    public InvalidJwtException(String message) {
        super(message);
    }

    public InvalidJwtException(String message, Throwable cause) {
        super(message, cause);
    }
}
