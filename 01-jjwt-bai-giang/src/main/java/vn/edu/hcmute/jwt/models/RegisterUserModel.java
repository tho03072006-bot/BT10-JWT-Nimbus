package vn.edu.hcmute.jwt.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RegisterUserModel {
    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @NotBlank
    @Size(min = 6, max = 72)
    private String password;

    @NotBlank
    @Size(max = 50)
    private String fullName;

    /** Tuy chon: neu bo trong se dung anh dai dien mac dinh (cot images cua Entity la NOT NULL). */
    @Size(max = 500)
    private String images;
}
