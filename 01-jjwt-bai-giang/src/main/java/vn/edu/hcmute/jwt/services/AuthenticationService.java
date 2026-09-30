package vn.edu.hcmute.jwt.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.hcmute.jwt.entity.User;
import vn.edu.hcmute.jwt.models.LoginUserModel;
import vn.edu.hcmute.jwt.models.RegisterUserModel;
import vn.edu.hcmute.jwt.repository.UserRepository;

@Service
public class AuthenticationService {

    static final String DEFAULT_IMAGE = "/images/default-avatar.svg";

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User signup(RegisterUserModel input) {
        User user = new User();
        user.setFullName(input.getFullName());
        user.setEmail(input.getEmail());
        user.setImages(input.getImages() == null || input.getImages().isBlank() ? DEFAULT_IMAGE : input.getImages());
        user.setPassword(passwordEncoder.encode(input.getPassword()));

        return userRepository.save(user);
    }

    /** Xac thuc bang AuthenticationManager (nem BadCredentialsException neu sai), roi lay User tu DB. */
    public User authenticate(LoginUserModel input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );

        return userRepository.findByEmail(input.getEmail())
                .orElseThrow();
    }
}
