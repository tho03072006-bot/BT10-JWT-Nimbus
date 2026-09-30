package vn.edu.hcmute.jwt;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.hcmute.jwt.entity.User;
import vn.edu.hcmute.jwt.services.JwtService;

/** Kich ban Postman cua bai giang (Buoc 9) va bang loi (Buoc 10), chay tren toan bo ung dung. */
@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtService jwtService;

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    private static String email() {
        return "sv-" + UUID.randomUUID().toString().substring(0, 8) + "@hcmute.edu.vn";
    }

    private String signupJson(String email, String password, String fullName) {
        return "{\"email\":\"%s\",\"password\":\"%s\",\"fullName\":\"%s\"}".formatted(email, password, fullName);
    }

    private String loginJson(String email, String password) {
        return "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password);
    }

    private void signup(String email, String password, String fullName) throws Exception {
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson(email, password, fullName)))
                .andExpect(status().isOk());
    }

    private String loginToken(String email, String password) throws Exception {
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    @Test
    void signupStoresBcryptHashAndNeverReturnsPassword() throws Exception {
        String email = email();

        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson(email, "123456", "Nguyễn Hữu Trung")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.fullName").value("Nguyễn Hữu Trung"))
                .andExpect(jsonPath("$.images").value("/images/default-avatar.svg"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void signupRejectsDuplicateEmailAndInvalidInput() throws Exception {
        String email = email();
        signup(email, "123456", "Trung");

        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson(email, "123456", "Trung")))
                .andExpect(status().isConflict());
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(signupJson("khong-phai-email", "123", "")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginReturnsTokenAndExpiresIn() throws Exception {
        String email = email();
        signup(email, "123456", "Trung");

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, "123456")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").value(3600000));
    }

    @Test
    void wrongPasswordOrUnknownEmailIs401() throws Exception {
        String email = email();
        signup(email, "123456", "Trung");

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, "sai-mat-khau")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("The username or password is incorrect"));
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("khong-ton-tai@hcmute.edu.vn", "123456")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meReturnsAuthenticatedUserFromTokenWithoutSession() throws Exception {
        String email = email();
        signup(email, "123456", "Nguyễn Hữu Trung");
        String token = loginToken(email, "123456");

        mvc.perform(get("/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.fullName").value("Nguyễn Hữu Trung"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(header().doesNotExist("Set-Cookie"));
    }

    @Test
    void usersListNeedsTokenAndListsRegisteredUsers() throws Exception {
        String email = email();
        signup(email, "123456", "Trung");
        String token = loginToken(email, "123456");

        mvc.perform(get("/users")).andExpect(status().isUnauthorized());
        mvc.perform(get("/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email=='%s')]".formatted(email), hasSize(1)))
                .andExpect(jsonPath("$[0].password").doesNotExist());
        mvc.perform(get("/users/").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void missingTokenIs401Json() throws Exception {
        mvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void tamperedTokenIs401InvalidSignature() throws Exception {
        String email = email();
        signup(email, "123456", "Trung");
        String[] parts = loginToken(email, "123456").split("\\.");
        String forgedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(
                new String(Base64.getUrlDecoder().decode(parts[1])).replace(email, "admin@hcmute.edu.vn").getBytes());

        mvc.perform(get("/users/me").header("Authorization", "Bearer " + parts[0] + "." + forgedPayload + "." + parts[2]))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("The JWT signature is invalid"));
    }

    @Test
    void expiredTokenIs401Expired() throws Exception {
        String email = email();
        signup(email, "123456", "Trung");
        User user = new User();
        user.setEmail(email);
        user.setPassword("x");
        JwtService shortLived = new JwtService();
        ReflectionTestUtils.setField(shortLived, "secretKey", secretKey);
        ReflectionTestUtils.setField(shortLived, "jwtExpiration", -1000L);

        mvc.perform(get("/users/me").header("Authorization", "Bearer " + shortLived.generateToken(user)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("The JWT token has expired"));
    }

    @Test
    void garbageTokenIs401() throws Exception {
        mvc.perform(get("/users/me").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validTokenOfUnknownUserIs401() throws Exception {
        User ghost = new User();
        ghost.setEmail("ma-quai@hcmute.edu.vn");
        ghost.setPassword("x");

        mvc.perform(get("/users/me").header("Authorization", "Bearer " + jwtService.generateToken(ghost)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sampleAccountsAreSeededAndShownOnLoginPage() throws Exception {
        for (var a : vn.edu.hcmute.jwt.configs.DemoAccounts.ACCOUNTS) {
            String token = loginToken(a.email(), a.password());
            mvc.perform(get("/users/me").header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fullName").value(a.fullName()));
            mvc.perform(get("/login")).andExpect(content().string(containsString(a.email())))
                    .andExpect(content().string(containsString(a.password())));
        }
    }

    @Test
    void pagesAreServedWithoutTokenAndUseMainJs() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"Login\"")))
                .andExpect(content().string(containsString("/js/mainjs.js")))
                .andExpect(content().string(containsString("/css/app.css")));
        mvc.perform(get("/css/app.css")).andExpect(status().isOk());
        mvc.perform(get("/user/profile")).andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"profile\"")));
        mvc.perform(get("/js/mainjs.js")).andExpect(status().isOk());
        mvc.perform(get("/images/default-avatar.svg")).andExpect(status().isOk());
        mvc.perform(get("/")).andExpect(status().is3xxRedirection());
    }
}
