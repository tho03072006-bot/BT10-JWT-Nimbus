# Bản 2 - thay JJWT bằng Nimbus JOSE + JWT

Bản này giữ luồng Spring Boot 3 / Spring Security 6 của ví dụ trong `04_JWT.pdf`, nhưng dùng `com.nimbusds:nimbus-jose-jwt` thay cho ba dependency JJWT. So sánh `pom.xml` và `src/main/java/vn/edu/hcmute/jwt/services/JwtService.java` với [bản JJWT](../01-jjwt-bai-giang/) để thấy phần thư viện đã thay.

## Chạy

Từ thư mục gốc repository (nơi có `mvnw.cmd`):

```powershell
.\mvnw.cmd -pl 02-nimbus-thay-the spring-boot:run
```

Mở `http://localhost:8006/login`. Mặc định dùng H2 trong bộ nhớ. Profile `mysql` dùng MySQL như bài giảng; xem `src/main/resources/application-mysql.properties`.

## Đối chiếu mẫu của bài giảng

- Trang 28–29: thử bằng Postman `POST /auth/signup`, `POST /auth/login`, rồi `GET /users/me` và `GET /users` với Bearer token.
- Trang 30–31: lỗi đăng nhập, chữ ký token và token hết hạn được ánh xạ về HTTP 401; lỗi quyền truy cập về 403.
- Trang 32–34: giữ luồng Ajax đăng nhập, lưu token trong `localStorage`, rồi gọi `/users/me` kèm Bearer token. Giao diện mở rộng có đăng ký, hồ sơ, thông tin JWT và phần gửi yêu cầu API để xem HTTP status cùng JSON.

Trang mẫu **không có form đăng ký**. Đăng ký là bước thử qua Postman như trang 28. Hai tài khoản demo được tự tạo để có thể thử đăng nhập ngay: `trungnh@hcmute.edu.vn` / `123456` và `sinhvien@hcmute.edu.vn` / `Demo@123`. Đặt `app.demo.enabled=false` để tắt dữ liệu demo.

Ví dụ gọi API:

```text
POST http://localhost:8006/auth/signup
{"email":"moi@hcmute.edu.vn","password":"123456","fullName":"Người dùng mới"}

POST http://localhost:8006/auth/login
{"email":"moi@hcmute.edu.vn","password":"123456"}

GET http://localhost:8006/users/me
Authorization: Bearer <token>

GET http://localhost:8006/users
Authorization: Bearer <token>
```

## Khác biệt thư viện

| JJWT trong bài giảng | Nimbus trong bản này |
|---|---|
| `Jwts.builder()` | `JWTClaimsSet.Builder` |
| `signWith(...)` | `SignedJWT` và `MACSigner` |
| `parseSignedClaims(...)` | `SignedJWT.parse(...)` và `MACVerifier` |
| Tự ném `ExpiredJwtException` | `JwtService` kiểm tra claim `exp` |

Chạy `.\mvnw.cmd -pl 02-nimbus-thay-the test` từ thư mục gốc để kiểm tra luồng API và JWT.
