package com.nazor.practicejava.auth;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nazor.practicejava.auth.dto.LoginRequest;
import com.nazor.practicejava.auth.dto.MessageResponse;
import com.nazor.practicejava.auth.dto.UserInfoResponse;
import com.nazor.practicejava.security.JwtUtils;
import com.nazor.practicejava.user.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * JWT authentication endpoints. Tokens are delivered as HttpOnly cookies:
 * a short-lived access token ({@code /api}) and a long-lived refresh token
 * (scoped to {@code /api/auth/refreshtoken}). The refresh token is persisted so
 * it can be revoked server-side on sign-out.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils,
                          RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/signin")
    public ResponseEntity<UserInfoResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = (User) authentication.getPrincipal();

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        ResponseCookie refreshCookie = jwtUtils.generateRefreshJwtCookie(refreshToken.getToken());

        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new UserInfoResponse(user.getId(), user.getEmail(), roles));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<MessageResponse> refreshToken(HttpServletRequest request) {
        String refreshToken = jwtUtils.getJwtRefreshFromCookies(request);
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new TokenRefreshException(String.valueOf(refreshToken), "Refresh token cookie is missing.");
        }

        return refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(user);
                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                            .body(new MessageResponse("Access token has been refreshed."));
                })
                .orElseThrow(() -> new TokenRefreshException(refreshToken,
                        "Refresh token is not in the database."));
    }

    @PostMapping("/signout")
    public ResponseEntity<MessageResponse> logoutUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            refreshTokenService.revokeByUser(user);
        }

        ResponseCookie jwtCookie = jwtUtils.getCleanJwtCookie();
        ResponseCookie refreshCookie = jwtUtils.getCleanJwtRefreshCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new MessageResponse("You have been signed out."));
    }
}
