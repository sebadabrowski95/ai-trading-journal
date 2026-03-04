package com.example.aitradingjournalbackend.auth;

import com.example.aitradingjournalbackend.auth.dto.ActivationConfirmRequest;
import com.example.aitradingjournalbackend.auth.dto.AuthResponse;
import com.example.aitradingjournalbackend.auth.dto.LoginRequest;
import com.example.aitradingjournalbackend.auth.dto.MeResponse;
import com.example.aitradingjournalbackend.auth.dto.MessageResponse;
import com.example.aitradingjournalbackend.auth.dto.PasswordResetConfirmRequest;
import com.example.aitradingjournalbackend.auth.dto.PasswordResetRequest;
import com.example.aitradingjournalbackend.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return new MessageResponse("Jeśli rejestracja jest możliwa, wysłaliśmy wiadomość aktywacyjną.");
    }

    @PostMapping("/activate")
    @ResponseStatus(HttpStatus.OK)
    public MessageResponse activate(@Valid @RequestBody ActivationConfirmRequest request) {
        authService.activateAccount(request.token());
        return new MessageResponse("Konto aktywowane.");
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.email(), loginRequest.password())
            );
        } catch (DisabledException | BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        } catch (AuthenticationServiceException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication failed");
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails.getUsername(), userDetails.getTokenVersion());
        return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds());
    }

    @PostMapping("/password-reset/request")
    @ResponseStatus(HttpStatus.OK)
    public MessageResponse requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request);
        return new MessageResponse("Jeśli konto istnieje, wysłaliśmy wiadomość z instrukcją resetu hasła.");
    }

    @PostMapping("/password-reset/confirm")
    @ResponseStatus(HttpStatus.OK)
    public MessageResponse confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request);
        return new MessageResponse("Hasło zostało zmienione.");
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public MeResponse me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return new MeResponse(authentication.getName());
    }
}
