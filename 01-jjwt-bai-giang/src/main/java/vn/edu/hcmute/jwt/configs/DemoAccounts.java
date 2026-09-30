package vn.edu.hcmute.jwt.configs;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.edu.hcmute.jwt.models.RegisterUserModel;
import vn.edu.hcmute.jwt.repository.UserRepository;
import vn.edu.hcmute.jwt.services.AuthenticationService;

/**
 * Tai khoan mau de test nhanh, tu tao khi khoi dong (bo qua neu email da ton tai, nen chay lai MySQL khong bi trung).
 * Chi de demo local: dat app.demo.enabled=false de tat, vua khong tao tai khoan vua khong hien thi tren trang login.
 */
@Configuration
public class DemoAccounts {

    public record DemoAccount(String fullName, String email, String password, String note) {
    }

    public static final List<DemoAccount> ACCOUNTS = List.of(
            new DemoAccount("Nguyễn Hữu Trung", "trungnh@hcmute.edu.vn", "123456", "Giảng viên (giống bài giảng)"),
            new DemoAccount("Trần Minh Thọ", "sinhvien@hcmute.edu.vn", "Demo@123", "Sinh viên"));

    @Bean
    ApplicationRunner seedDemoAccounts(
            @Value("${app.demo.enabled:true}") boolean enabled,
            UserRepository users,
            AuthenticationService authenticationService) {
        return args -> {
            if (!enabled) {
                return;
            }
            for (DemoAccount a : ACCOUNTS) {
                if (users.findByEmail(a.email()).isEmpty()) {
                    authenticationService.signup(new RegisterUserModel(a.email(), a.password(), a.fullName(), null));
                }
            }
        };
    }
}
