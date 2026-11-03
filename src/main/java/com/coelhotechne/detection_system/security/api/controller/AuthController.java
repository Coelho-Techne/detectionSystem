package com.coelhotechne.detection_system.security.api.controller;

import com.coelhotechne.detection_system.security.api.dto.LoginRequest;
import com.coelhotechne.detection_system.security.api.dto.LoginResponse;
import com.coelhotechne.detection_system.security.api.dto.PasswordChangeRequest;
import com.coelhotechne.detection_system.security.api.dto.UserResponse;
import com.coelhotechne.detection_system.security.application.UserService;
import com.coelhotechne.detection_system.security.domain.enums.Role;
import com.coelhotechne.detection_system.security.domain.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@Tag(name = "Auth")
@RequestMapping(value = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthController {

    private static final String ROLE_PREFIX = "ROLE_";

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService service;

    @Operation(summary = "Autentica um usuário e devolve um JWT")
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken
                            .unauthenticated(request.email().trim().toLowerCase(Locale.ROOT), request.password()));
        } catch (AuthenticationException e) {
            throw new ErrorResponseException(HttpStatus.UNAUTHORIZED, e);
        }

        String email = authentication.getName();
        Role role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(ROLE_PREFIX))
                .map(authority -> authority.substring(ROLE_PREFIX.length()))
                .map(Role::valueOf)
                .findFirst()
                .orElseThrow(() -> new ErrorResponseException(HttpStatus.UNAUTHORIZED));


        String token = jwtTokenProvider.generateToken(email, role);
        return ResponseEntity.status(HttpStatus.OK).body(LoginResponse.bearer(token, email, role.toString()));
    }
    @Operation(summary = "Dados do usuario autenticado")
    @PreAuthorize(UserAccess.AUTH)
    @GetMapping("/me")
    public ResponseEntity<UserResponse>me(Authentication authentication){
        return ResponseEntity.ok(service.me(authentication.getName()));
    }

    @Operation(summary = "Troca a propria senha (exige senha atual)")
    @PreAuthorize(UserAccess.AUTH)
    @PostMapping(value = "/password",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request,
                                               Authentication authentication){
        service.changeOwnPassword(authentication.getName(),request);
        return ResponseEntity.noContent().build();
    }
}
