package vn.edu.hcmute.jwt.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginResponse {
    private String token;

    /** Thoi gian song cua token, don vi mili giay (vd 3600000 = 1 gio). */
    private long expiresIn;
}
