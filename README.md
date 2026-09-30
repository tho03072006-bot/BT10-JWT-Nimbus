# BT10 - hai bản JWT trong cùng repository

Bài tập từ `04_JWT.pdf` được trình bày thành **hai dự án Maven độc lập**:

| Thư mục | Mục đích | Thư viện |
|---|---|---|
| [`01-jjwt-bai-giang`](01-jjwt-bai-giang/) | Ví dụ Spring Boot 3, Security 6 theo bài giảng | `io.jsonwebtoken:jjwt-*` 0.12.6 |
| [`02-nimbus-thay-the`](02-nimbus-thay-the/) | Cùng chức năng, thay thư viện JWT của bài giảng | `com.nimbusds:nimbus-jose-jwt` 10.10 |

**JWT là định dạng token**; JJWT và Nimbus là hai thư viện Java tạo và xác minh token. Hai bản dùng cùng API và giao diện để có thể so sánh trực tiếp `pom.xml` và `JwtService.java`. Bản Nimbus là dự án đã có trước khi tách.

## Chạy ứng dụng

Cần Java 21. Từ thư mục gốc, có thể chạy **hai bản cùng lúc**: JJWT dùng cổng `8005`, Nimbus dùng cổng `8006`:

```powershell
.\mvnw.cmd -pl 01-jjwt-bai-giang spring-boot:run
```

hoặc:

```powershell
.\mvnw.cmd -pl 02-nimbus-thay-the spring-boot:run
```

Mở JJWT tại `http://localhost:8005/login`, Nimbus tại `http://localhost:8006/login`. Mặc định dùng H2 trong bộ nhớ; cả hai có profile `mysql`. Trong Eclipse/STS, import hai thư mục con bằng **Existing Maven Projects**, rồi chạy `JwtSpringboot3Application` của bản muốn xem.

## Đối chiếu yêu cầu

Cả hai bản có `POST /auth/signup`, `POST /auth/login` trả `token` và `expiresIn`, `GET /users/me`, `GET /users` với Bearer token, lỗi 401 khi thiếu hoặc sai token, và trang Ajax `/login`, `/user/profile`. Giao diện giữ bản đầy đủ với đăng nhập, đăng ký, thông tin JWT và phần thử API. Phần thử API cho phép chọn `/users/me` hoặc `/users`, dán Bearer token và xem HTTP status cùng JSON như ví dụ Postman. Mật khẩu đã mã hóa không được trả ra JSON.

| Công việc | JJWT | Nimbus |
|---|---|---|
| Tạo claims và ký HS256 | `Jwts.builder()`, `signWith(...)` | `JWTClaimsSet.Builder`, `SignedJWT`, `MACSigner` |
| Xác minh | `Jwts.parser().verifyWith(...).parseSignedClaims(...)` | `SignedJWT.parse(...)`, `MACVerifier` |
| Hết hạn | JJWT ném `ExpiredJwtException` | `JwtService` kiểm tra `exp` |

Kiểm thử cả hai từ thư mục gốc bằng `.\mvnw.cmd test`. Xem thêm [hướng dẫn bản Nimbus](02-nimbus-thay-the/README.md).

## Chạy nhanh trên Windows và sửa lỗi Eclipse/STS

Nhấp đúp **RUN-JJWT.cmd** hoặc **RUN-NIMBUS.cmd** ở thư mục gốc. Giữ cửa sổ đó mở, chờ dòng `Started JwtSpringboot3Application`, rồi vào http://localhost:8005/login (JJWT) hoặc http://localhost:8006/login (Nimbus). Hai bản có thể chạy đồng thời; mỗi bản giữ token riêng theo cổng.

Tài khoản mẫu: `trungnh@hcmute.edu.vn` / `123456` hoặc `sinhvien@hcmute.edu.vn` / `Demo@123`.

Nếu Eclipse vẫn hiện một project `BT_10` với hai thư mục con, cấu hình Java cũ đang trỏ vào `BT_10/src/main/java`, trong khi mã nguồn nằm trong từng module. Chọn file Java trong thư mục con chưa đủ để sửa classpath.

1. Xóa project `BT_10` khỏi workspace bằng **Delete**, bỏ chọn **Delete project contents on disk** để giữ toàn bộ file.
2. Chọn **File → Import → Maven → Existing Maven Projects**. Chọn thư mục `BT_10` và đánh dấu hai POM trong `01-jjwt-bai-giang` và `02-nimbus-thay-the` (không cần chọn POM gốc).
3. Hai project riêng sẽ xuất hiện: `jwt-springboot3-jjwt` và `jwt-springboot3-nimbus`. Chọn chúng → **Maven → Update Project**, sau đó **Project → Clean**.
4. Trong project cần chạy, mở `src/main/java/vn/edu/hcmute/jwt/JwtSpringboot3Application.java` → **Run As → Spring Boot App**. Không dùng lại cấu hình Run cũ có project `BT_10`.

POM gốc có `packaging=pom`, dùng để gom hai module và chạy kiểm thử; ứng dụng Spring Boot nằm trong mỗi module.
