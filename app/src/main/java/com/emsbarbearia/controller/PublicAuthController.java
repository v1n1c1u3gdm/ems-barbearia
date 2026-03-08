package com.emsbarbearia.controller;

import com.emsbarbearia.config.PublicAuthProperties;
import com.emsbarbearia.dto.ClienteResponse;
import com.emsbarbearia.dto.PublicLoginRequest;
import com.emsbarbearia.dto.PublicRegisterRequest;
import com.emsbarbearia.dto.PublicTokenResponse;
import com.emsbarbearia.repository.ClienteRepository;
import com.emsbarbearia.dto.PhoneOtpRequest;
import com.emsbarbearia.dto.VerifyOtpRequest;
import com.emsbarbearia.service.AppleOAuthService;
import com.emsbarbearia.service.GoogleOAuthService;
import com.emsbarbearia.service.OtpService;
import com.emsbarbearia.service.PublicAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/public")
@Tag(name = "Public Auth", description = "Cliente registration, login, OAuth, and me (JWT)")
public class PublicAuthController {

    private final PublicAuthService publicAuthService;
    private final ClienteRepository clienteRepository;
    private final GoogleOAuthService googleOAuthService;
    private final AppleOAuthService appleOAuthService;
    private final OtpService otpService;
    private final PublicAuthProperties publicAuthProperties;

    public PublicAuthController(PublicAuthService publicAuthService, ClienteRepository clienteRepository,
                               GoogleOAuthService googleOAuthService, AppleOAuthService appleOAuthService,
                               OtpService otpService, PublicAuthProperties publicAuthProperties) {
        this.publicAuthService = publicAuthService;
        this.clienteRepository = clienteRepository;
        this.googleOAuthService = googleOAuthService;
        this.appleOAuthService = appleOAuthService;
        this.otpService = otpService;
        this.publicAuthProperties = publicAuthProperties;
    }

    @PostMapping("/register")
    @Operation(summary = "Register with nome, email, senha; returns JWT")
    public ResponseEntity<PublicTokenResponse> register(@Valid @RequestBody PublicRegisterRequest request) {
        String token = publicAuthService.register(request);
        return ResponseEntity.ok(new PublicTokenResponse(token));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email, senha; returns JWT")
    public ResponseEntity<PublicTokenResponse> login(@Valid @RequestBody PublicLoginRequest request) {
        String token = publicAuthService.login(request);
        return ResponseEntity.ok(new PublicTokenResponse(token));
    }

    @GetMapping("/me")
    @Operation(summary = "Current cliente (requires Bearer JWT)")
    public ResponseEntity<ClienteResponse> me(@AuthenticationPrincipal Long clienteId) {
        if (clienteId == null) {
            return ResponseEntity.status(401).build();
        }
        return clienteRepository.findById(clienteId)
            .map(publicAuthService::toResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/oauth/google")
    @Operation(summary = "Redirect to Google OAuth")
    public ResponseEntity<Void> oauthGoogleRedirect(HttpServletRequest request) {
        String callbackUrl = publicAuthProperties.getBackendBaseUrl() + "/auth/public/oauth/google/callback";
        String authUrl = googleOAuthService.buildAuthorizationUrl(callbackUrl);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(authUrl)).build();
    }

    @GetMapping("/oauth/google/callback")
    @Operation(summary = "Google OAuth callback; redirects to frontend with token")
    public ResponseEntity<Void> oauthGoogleCallback(@RequestParam String code) {
        String callbackUrl = publicAuthProperties.getBackendBaseUrl() + "/auth/public/oauth/google/callback";
        String token = googleOAuthService.exchangeCodeAndCreateToken(code, callbackUrl);
        String frontendRedirect = publicAuthProperties.getFrontendUrl() + "/agendar?token=" + token;
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(frontendRedirect)).build();
    }

    @GetMapping("/oauth/apple")
    @Operation(summary = "Redirect to Sign in with Apple")
    public ResponseEntity<Void> oauthAppleRedirect() {
        String callbackUrl = publicAuthProperties.getBackendBaseUrl() + "/auth/public/oauth/apple/callback";
        String authUrl = appleOAuthService.buildAuthorizationUrl(callbackUrl);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(authUrl)).build();
    }

    @PostMapping(value = "/oauth/apple/callback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Operation(summary = "Apple OAuth callback (form_post); redirects to frontend with token")
    public ResponseEntity<Void> oauthAppleCallback(@RequestParam String code) {
        String callbackUrl = publicAuthProperties.getBackendBaseUrl() + "/auth/public/oauth/apple/callback";
        String token = appleOAuthService.exchangeCodeAndCreateToken(code, callbackUrl);
        String frontendRedirect = publicAuthProperties.getFrontendUrl() + "/agendar?token=" + token;
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(frontendRedirect)).build();
    }

    @PostMapping("/phone/request-otp")
    @Operation(summary = "Request OTP sent to phone (SMS/WhatsApp); rate limited")
    public ResponseEntity<Void> requestOtp(@Valid @RequestBody PhoneOtpRequest request) {
        otpService.requestOtp(request.telefone());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/phone/verify-otp")
    @Operation(summary = "Verify OTP; returns JWT")
    public ResponseEntity<PublicTokenResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        String token = otpService.verifyOtp(request);
        return ResponseEntity.ok(new PublicTokenResponse(token));
    }
}
