package com.shehia_management.api.auth;

import com.shehia_management.api.identity.User;
import com.shehia_management.api.identity.UserStatus;
import com.shehia_management.api.identity.UserRepository;
import com.shehia_management.api.shared.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        // 1. Look up the account first so we can give an accurate reason for
        // rejecting a login (wrong credentials vs. pending approval vs.
        // suspended) instead of Spring Security's generic "disabled account"
        // exception (which is indistinguishable from a bad password once it
        // reaches the generic catch below).
        User user = userRepository.findByZanId(loginRequest.getZanId()).orElse(null);

        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid credentials", "Authentication failed"));
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Account suspended", "Your account has been suspended by administrator"));
        }

        if (user.getStatus() == UserStatus.PENDING) {
            String message = user.getRole() == com.shehia_management.api.identity.Role.ROLE_STAFF
                    ? "Your staff account is awaiting zone assignment and approval by an administrator."
                    : "Your account is awaiting verification by the Shehia office.";
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Account pending approval", message));
        }

        // 2. Credentials and status are both fine — run the request through
        // Spring Security too so the SecurityContext/authorities are set up
        // exactly the way the rest of the app expects.
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getZanId(),
                            loginRequest.getPassword()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid credentials", "Authentication failed"));
        }

        // 3. Generate JWT token
        String token = tokenProvider.generateToken(user.getZanId());

        // 4. Build response
        LoginResponse response = new LoginResponse(
                token,
                user.getZanId(),
                user.getRole().toString(),
                user.isMustChangePassword()
        );

        return ResponseEntity.ok(response);
    }


    // ============================================================
    // CHANGE PASSWORD
    // ============================================================

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        User user = userRepository
                .findByZanId(authentication.getName())
                .orElse(null);


        // User does not exist
        if (user == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "User not found",
                            "No user was found with the supplied ZanID"
                    ));
        }


        // Check current password
        if (request.getCurrentPassword() == null ||
                !passwordEncoder.matches(
                        request.getCurrentPassword(),
                        user.getPassword())) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(
                            "Invalid current password",
                            "The current password is incorrect"
                    ));
        }


        // Validate new password
        if (request.getNewPassword() == null ||
                request.getNewPassword().isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(
                            "Invalid password",
                            "New password cannot be empty"
                    ));
        }


        // Prevent using same password
        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword())) {

            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(
                            "Invalid password",
                            "New password must be different from the current password"
                    ));
        }


        // Save new BCrypt password
        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );


        // First-login requirement completed
        user.setMustChangePassword(false);


        userRepository.save(user);


        return ResponseEntity.ok(
                new SuccessResponse(
                        "Password changed successfully. You can now access the system."
                )
        );
    }


    // ============================================================
    // LOGOUT
    // ============================================================

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {

        return ResponseEntity.ok(
                new SuccessResponse(
                        "Logged out successfully"
                )
        );
    }


    // ============================================================
    // SUCCESS RESPONSE
    // ============================================================

    public static class SuccessResponse {

        public String message;

        public SuccessResponse(String message) {
            this.message = message;
        }
    }


    // ============================================================
    // ERROR RESPONSE
    // ============================================================

    public static class ErrorResponse {

        public String error;
        public String message;

        public ErrorResponse(
                String error,
                String message
        ) {
            this.error = error;
            this.message = message;
        }
    }
}