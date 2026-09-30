# BT10 - JWT với Spring Boot 3 + Security 6, dùng Nimbus JOSE + JWT

Bài tập môn Lập trình Web (bài giảng `04_JWT.pdf`, ThS. Nguyễn Hữu Trung):

1. Làm bài tập ví dụ "Demo JWT với Spring Boot 3 - Security 6" (10 bước, trang 15-34).
2. Dùng thư viện **Nimbus JOSE + JWT** thay cho thư viện JWT của bài giảng (`io.jsonwebtoken:jjwt`).

Công nghệ: Java 21, Spring Boot 3.5.16, Spring Security 6.5, Spring Data JPA, Thymeleaf, Nimbus JOSE + JWT 10.10, H2 (mặc định) hoặc MySQL, Lombok, jQuery và CSS riêng.

## Chạy ứng dụng

Các lệnh dưới đây chạy từ **thư mục gốc repository** (nơi có `mvnw.cmd`).

```bash
.\mvnw.cmd -pl 02-nimbus-thay-the spring-boot:run
```

Mở http://localhost:8005/login (cổng 8005 như bài giảng).

Mặc định dùng H2 trong bộ nhớ nên chạy ngay, không cần cài cơ sở dữ liệu (dữ liệu mất khi tắt app). Muốn dùng MySQL như bài giảng:

```bash
.\mvnw.cmd -pl 02-nimbus-thay-the spring-boot:run -Dspring-boot.run.profiles=mysql
```

Profile `mysql` đọc `DB_USERNAME` (mặc định `root`) và `DB_PASSWORD`, tự tạo schema `jwt_springboot3` (`createDatabaseIfNotExist=true`).

| Biến môi trường | Ý nghĩa |
|-----------------|---------|
| `JWT_SECRET`    | Secret HS256 dạng Base64/hex, tối thiểu 256 bit. Có giá trị mặc định chỉ để demo local, khi triển khai thật phải đổi. |
| `DB_USERNAME`, `DB_PASSWORD` | Chỉ dùng với profile `mysql` |

## Tài khoản mẫu để test

Tự tạo khi khởi động và hiển thị ngay trên trang đăng nhập (bấm "Dùng" để điền sẵn):

| Họ tên | Email | Mật khẩu |
|--------|-------|----------|
| Nguyễn Hữu Trung | `trungnh@hcmute.edu.vn` | `123456` |
| Trần Minh Thọ | `sinhvien@hcmute.edu.vn` | `Demo@123` |

Chỉ dùng để demo. Khi triển khai thật đặt `app.demo.enabled=false` để không tạo tài khoản và không hiển thị chúng.

## Giao diện

- Trang đăng nhập: hai tab Đăng nhập / Đăng ký, nút Hiện/Ẩn mật khẩu, thông báo lỗi ngay tại form, thẻ tài khoản mẫu.
- Trang cá nhân: thông tin người dùng và khu "Kiểm tra API của bài lab" (có token, không token, token bị sửa payload) để thấy 200/401. Phần giải thích JWT, token thô, thời gian hết hạn và nút sao chép nằm trong mục mở rộng dành cho học tập.
- Tự đổi sáng/tối theo hệ thống, dùng được trên điện thoại, điều khiển được bằng bàn phím.

## Kịch bản demo (bước 9 và 10 của bài giảng)

1. Vào `/login`, tạo tài khoản ở tab "Đăng ký" (`POST /auth/signup`) hoặc dùng tài khoản mẫu.
2. Đăng nhập (`POST /auth/login`) nhận `{"token": "...", "expiresIn": 3600000}`. Token lưu trong `localStorage`, trang chuyển sang `/user/profile`.
3. Trang profile gọi Ajax `GET /users/me` kèm `Authorization: Bearer <token>` và hiển thị họ tên, ảnh.
4. Logout xóa token và quay về `/login`.

Hoặc dùng curl/Postman:

```bash
curl -X POST localhost:8005/auth/signup -H "Content-Type: application/json" \
     -d '{"email":"trungnh@hcmute.edu.vn","password":"123456","fullName":"Nguyễn Hữu Trung"}'

curl -X POST localhost:8005/auth/login -H "Content-Type: application/json" \
     -d '{"email":"trungnh@hcmute.edu.vn","password":"123456"}'

curl localhost:8005/users/me -H "Authorization: Bearer <token>"
curl localhost:8005/users    -H "Authorization: Bearer <token>"
```

## Đối chiếu từng bước của bài giảng

| Bước | Nội dung bài giảng | File |
|------|--------------------|------|
| 1 | Thêm dependency | `pom.xml` (jjwt-api/impl/jackson được thay bằng `nimbus-jose-jwt`) |
| 2 | Entity `User implements UserDetails` | `entity/User.java` |
| 3 | Models `LoginResponse`, `LoginUserModel`, `RegisterUserModel` | `models/` |
| 4 | `UserRepository`, `UserService`, `AuthenticationService`, `JwtService`, `application.properties` | `repository/`, `services/`, `src/main/resources/application.properties` |
| 5 | `ApplicationConfiguration` (UserDetailsService, BCrypt, AuthenticationManager, AuthenticationProvider) | `configs/ApplicationConfiguration.java` |
| 6 | `JwtAuthenticationFilter` | `filter/JwtAuthenticationFilter.java` |
| 7 | `SecurityConfiguration` (stateless, CORS, permitAll `/auth/**`, `/login**`, `/user/**`, `/images/**`, `/js/**`) | `configs/SecurityConfiguration.java` |
| 8 | `AuthenticationController` (`/auth/signup`, `/auth/login`), `UserController` (`/users/me`, `/users`) | `controllers/` |
| 9 | Test Postman: signup, login, `/users/me`, `/users` | `AuthFlowIntegrationTest` |
| 10 | Bảng exception (401/403) | `exception/GlobalExceptionHandler.java` |
| 10 | `login.html`, `profile.html`, `mainjs.js` (Ajax) | `templates/`, `static/js/mainjs.js`, `controllers/WebController.java` |

Bảng mã lỗi (bước 10):

| Lỗi xác thực | Ngoại lệ | HTTP |
|--------------|----------|------|
| Thông tin đăng nhập không hợp lệ | `BadCredentialsException` | 401 |
| Tài khoản bị khóa | `AccountStatusException` | 403 |
| Không được phép truy cập tài nguyên | `AccessDeniedException` | 403 |
| JWT không hợp lệ | `JwtSignatureException` (thay `SignatureException` của jjwt) | 401 |
| JWT đã hết hạn | `JwtExpiredException` (thay `ExpiredJwtException` của jjwt) | 401 |

## Thay jjwt bằng Nimbus (yêu cầu 2)

`JwtService` giữ nguyên tên hàm của bài giảng (`extractUsername`, `extractClaim`, `generateToken`, `getExpirationTime`, `isTokenValid`); chỉ phần gọi thư viện thay đổi:

| jjwt (bài giảng) | Nimbus (bài làm) |
|------------------|------------------|
| `Jwts.builder().subject().issuedAt().expiration()` | `JWTClaimsSet.Builder` |
| `.signWith(getSignInKey(), Jwts.SIG.HS256).compact()` | `new SignedJWT(new JWSHeader(HS256), claims)`, `jwt.sign(new MACSigner(key))`, `jwt.serialize()` |
| `Jwts.parser().verifyWith(key).build().parseSignedClaims(token)` | `SignedJWT.parse(token)`, `jwt.verify(new MACVerifier(key))`, `getJWTClaimsSet()` |
| `Decoders.BASE64.decode(secretKey)` | `Base64.getDecoder().decode(secretKey)` |
| `SignatureException` / `ExpiredJwtException` | `JwtSignatureException` / `JwtExpiredException` |

Khác với jjwt, Nimbus không tự kiểm tra hết hạn nên `extractAllClaims` tự kiểm tra `exp`, đồng thời chỉ chấp nhận thuật toán HS256 (chặn `alg: none`).

## Chỗ khác với bài giảng (có chủ đích)

- **Thời hạn token**: slide 20 ghi cứng 30 giờ trong `buildToken` dù `application.properties` đặt 1 giờ. Bài làm dùng đúng `security.jwt.expiration-time` (3600000 ms) nên `expiresIn` khớp với thực tế.
- **Không trả mật khẩu**: `User.password` có `@JsonIgnore` (slide 28 để lộ mã BCrypt trong JSON).
- **Cột `images` NOT NULL**: `RegisterUserModel` không có ảnh nên bài làm gán ảnh mặc định `/images/default-avatar.svg` (có thể truyền `images` khi đăng ký).
- **Chống XSS**: `mainjs.js` dùng `.text()`/`.attr()` thay `.html()` khi hiển thị `fullName`, `images`.
- **Ajax**: chỉ gọi `/users/me` ở trang profile, khi 401 thì xóa token và chuyển về `/login` thay vì `alert`.
- **Thêm** tab đăng ký trên `login.html`, tài khoản mẫu, trang cá nhân hiển thị token, validate đầu vào (400) và email trùng (409).
- **H2 mặc định** để chạy được ngay; MySQL dùng qua profile `mysql`. Profile `mysql` chưa được chạy thử trên máy này vì chưa có MySQL.
- jQuery tải từ CDN, không kèm thuộc tính SRI (`integrity`).

## Kiểm thử

```bash
.\mvnw.cmd -pl 02-nimbus-thay-the test
```

24 test:

- `JwtServiceTest` (10): sinh token HS256, claim `sub`/`exp`, claim bổ sung, `isTokenValid`, payload bị sửa, sai secret, hết hạn, `alg: none`, HS384, token rác, secret ngắn hơn 256 bit.
- `AuthFlowIntegrationTest` (13): signup (không lộ password, email trùng 409, dữ liệu sai 400), login (đúng, sai mật khẩu 401), `/users/me`, `/users/`, thiếu token 401, token bị sửa 401, token hết hạn 401, token rác 401, token của user không tồn tại 401, tài khoản mẫu đăng nhập được, các trang HTML và tài nguyên tĩnh.
- `DemoDisabledTest` (1): `app.demo.enabled=false` thì trang login không hiện tài khoản mẫu.
