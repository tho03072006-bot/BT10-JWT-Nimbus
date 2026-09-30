package vn.edu.hcmute.jwt;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.hcmute.jwt.configs.DemoAccounts;

@SpringBootTest(properties = "app.demo.enabled=false")
@AutoConfigureMockMvc
class DemoDisabledTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void loginPageHidesSampleAccountsWhenDisabled() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk())
                .andExpect(content().string(not(containsString(DemoAccounts.ACCOUNTS.get(0).email()))))
                .andExpect(content().string(not(containsString("Tài khoản mẫu"))));
    }
}
