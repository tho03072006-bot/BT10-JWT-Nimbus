package vn.edu.hcmute.jwt.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Buoc 10: anh xa loi xac thuc sang ma HTTP theo bang trong bai giang. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Thong tin dang nhap khong hop le -> 401. */
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException e) {
        return problem(HttpStatus.UNAUTHORIZED, "The username or password is incorrect", "Sai email hoặc mật khẩu");
    }

    /** Tai khoan bi khoa -> 403. */
    @ExceptionHandler(AccountStatusException.class)
    public ProblemDetail handleAccountStatus(AccountStatusException e) {
        return problem(HttpStatus.FORBIDDEN, "The account is locked", "Tài khoản bị khóa");
    }

    /** Khong duoc phep truy cap tai nguyen -> 403. */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException e) {
        return problem(HttpStatus.FORBIDDEN, "You are not authorized to access this resource",
                "Không có quyền truy cập tài nguyên này");
    }

    /** JWT het han -> 401. */
    @ExceptionHandler(JwtExpiredException.class)
    public ProblemDetail handleExpiredJwt(JwtExpiredException e) {
        return problem(HttpStatus.UNAUTHORIZED, "The JWT token has expired", "JWT đã hết hạn");
    }

    /** JWT khong hop le (chu ky sai) -> 401. */
    @ExceptionHandler(JwtSignatureException.class)
    public ProblemDetail handleJwtSignature(JwtSignatureException e) {
        return problem(HttpStatus.UNAUTHORIZED, "The JWT signature is invalid", "JWT không hợp lệ (sai chữ ký)");
    }

    /** JWT sai dinh dang -> 401. */
    @ExceptionHandler(InvalidJwtException.class)
    public ProblemDetail handleInvalidJwt(InvalidJwtException e) {
        return problem(HttpStatus.UNAUTHORIZED, "The JWT token is malformed", "JWT không hợp lệ (sai định dạng)");
    }

    /** Chua dang nhap / thieu token (do AuthenticationEntryPoint chuyen sang) -> 401. */
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException e) {
        return problem(HttpStatus.UNAUTHORIZED, "Authentication is required", "Cần đăng nhập (thiếu hoặc sai token)");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException e) {
        String fields = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .sorted()
                .reduce((a, b) -> a + "; " + b).orElse("invalid");
        return problem(HttpStatus.BAD_REQUEST, "Validation failed", fields);
    }

    /** Email da ton tai (cot email UNIQUE) -> 409. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDuplicate(DataIntegrityViolationException e) {
        return problem(HttpStatus.CONFLICT, "Email already registered", "Email đã được đăng ký");
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        return pd;
    }
}
