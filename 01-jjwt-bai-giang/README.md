# Bản 1 - ví dụ JWT dùng JJWT

Bản này thực hiện ví dụ Spring Boot 3 và Security 6 trong bài giảng `04_JWT.pdf` bằng ba dependency `jjwt-api`, `jjwt-impl`, `jjwt-jackson` phiên bản 0.12.6. Các API được giữ giống bản Nimbus để so sánh `pom.xml` và `JwtService.java` trực tiếp.

Chạy từ thư mục gốc repository:

```powershell
.\mvnw.cmd -pl 01-jjwt-bai-giang spring-boot:run
```

Truy cập `http://localhost:8005/login`. Có thể thử bằng Postman: `POST /auth/signup`, `POST /auth/login`, rồi gửi Bearer token đến `GET /users/me` và `GET /users`. Mặc định dùng H2; profile `mysql` dành cho MySQL như bài giảng.
