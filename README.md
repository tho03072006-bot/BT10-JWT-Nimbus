# BT10 - hai bản JWT trong cùng repository

Bài tập từ `04_JWT.pdf` được trình bày thành **hai dự án Maven độc lập**:

| Thư mục | Mục đích | Thư viện |
|---|---|---|
| [`01-jjwt-bai-giang`](01-jjwt-bai-giang/) | Ví dụ Spring Boot 3, Security 6 theo bài giảng | `io.jsonwebtoken:jjwt-*` 0.12.6 |
| [`02-nimbus-thay-the`](02-nimbus-thay-the/) | Cùng chức năng, thay thư viện JWT của bài giảng | `com.nimbusds:nimbus-jose-jwt` 10.10 |

**JWT là định dạng token**; JJWT và Nimbus là hai thư viện Java tạo và xác minh token. Hai bản dùng cùng API và giao diện để có thể so sánh trực tiếp `pom.xml` và `JwtService.java`. Bản Nimbus là dự án đã có trước khi tách.

## Chạy ứng dụng

Cần Java 21. Từ thư mục gốc, chạy **một bản tại một thời điểm** vì cả hai mặc định dùng cổng `8005`:

```powershell
.\mvnw.cmd -pl 01-jjwt-bai-giang spring-boot:run
```

hoặc:

```powershell
.\mvnw.cmd -pl 02-nimbus-thay-the spring-boot:run
```

Mở `http://localhost:8005/login`. Mặc định dùng H2 trong bộ nhớ; cả hai có profile `mysql`. Trong Eclipse/STS, import hai thư mục con bằng **Existing Maven Projects**, rồi chạy `JwtSpringboot3Application` của bản muốn xem.

## Đối chiếu yêu cầu

Cả hai bản có `POST /auth/signup`, `POST /auth/login` trả `token` và `expiresIn`, `GET /users/me`, `GET /users` với Bearer token, lỗi 401 khi thiếu hoặc sai token, và trang Ajax `/login`, `/user/profile`. Mật khẩu đã mã hóa không được trả ra JSON.

| Công việc | JJWT | Nimbus |
|---|---|---|
| Tạo claims và ký HS256 | `Jwts.builder()`, `signWith(...)` | `JWTClaimsSet.Builder`, `SignedJWT`, `MACSigner` |
| Xác minh | `Jwts.parser().verifyWith(...).parseSignedClaims(...)` | `SignedJWT.parse(...)`, `MACVerifier` |
| Hết hạn | JJWT ném `ExpiredJwtException` | `JwtService` kiểm tra `exp` |

Kiểm thử cả hai từ thư mục gốc bằng `.\mvnw.cmd test`. Xem thêm [hướng dẫn bản Nimbus](02-nimbus-thay-the/README.md).
