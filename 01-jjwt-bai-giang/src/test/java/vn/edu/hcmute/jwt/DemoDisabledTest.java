package vn.edu.hcmute.jwt;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.hcmute.jwt.configs.DemoAccounts;

@SpringBootTest(properties = {
        "app.demo.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:jwt_demo_disabled;MODE=MySQL;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
class DemoDisabledTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void sampleAccountIsNotCreatedWhenDemoIsDisabled() throws Exception {
        var account = DemoAccounts.ACCOUNTS.get(0);
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}"
                                .formatted(account.email(), account.password())))
                .andExpect(status().isUnauthorized());
    }
}
