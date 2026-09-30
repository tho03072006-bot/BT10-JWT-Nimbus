package vn.edu.hcmute.jwt.models;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginUserModel {
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
