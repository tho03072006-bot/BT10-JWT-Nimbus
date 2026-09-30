package vn.edu.hcmute.jwt.exception;

/** Tuong duong SignatureException cua jjwt: chu ky khong khop hoac thuat toan khong duoc chap nhan. */
public class JwtSignatureException extends InvalidJwtException {

    public JwtSignatureException(String message) {
        super(message);
    }
}
