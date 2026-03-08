package com.emsbarbearia.service;

import com.emsbarbearia.config.PublicAuthProperties;
import com.emsbarbearia.dto.GoogleUserInfo;
import com.emsbarbearia.entity.Cliente;
import com.emsbarbearia.entity.ClienteCredential;
import com.emsbarbearia.repository.ClienteCredentialRepository;
import com.emsbarbearia.repository.ClienteRepository;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class GoogleOAuthService {

    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
    private static final String PROVIDER_GOOGLE = "GOOGLE";
    private static final String SCOPE = "openid email profile";

    private final PublicAuthProperties properties;
    private final ClienteRepository clienteRepository;
    private final ClienteCredentialRepository credentialRepository;
    private final JwtPublicService jwtPublicService;
    private final RestTemplate restTemplate = new RestTemplate();

    public GoogleOAuthService(PublicAuthProperties properties,
                             ClienteRepository clienteRepository,
                             ClienteCredentialRepository credentialRepository,
                             JwtPublicService jwtPublicService) {
        this.properties = properties;
        this.clienteRepository = clienteRepository;
        this.credentialRepository = credentialRepository;
        this.jwtPublicService = jwtPublicService;
    }

    public String buildAuthorizationUrl(String redirectUri) {
        String clientId = properties.getGoogle().getClientId();
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("Google OAuth client-id not configured");
        }
        String scope = URLEncoder.encode(SCOPE, StandardCharsets.UTF_8);
        String encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        return GOOGLE_AUTH_URL + "?client_id=" + clientId + "&redirect_uri=" + encodedRedirect
            + "&response_type=code&scope=" + scope + "&access_type=offline";
    }

    public String exchangeCodeAndCreateToken(String code, String redirectUri) {
        String accessToken = exchangeCodeForAccessToken(code, redirectUri);
        GoogleUserInfo userInfo = fetchUserInfo(accessToken);
        Cliente cliente = findOrCreateCliente(userInfo);
        ClienteCredential cred = credentialRepository.findByProviderAndExternalId(PROVIDER_GOOGLE, userInfo.getSub())
            .orElseGet(() -> {
                ClienteCredential c = new ClienteCredential();
                c.setCliente(cliente);
                c.setProvider(PROVIDER_GOOGLE);
                c.setExternalId(userInfo.getSub());
                c.setPasswordHash(null);
                return credentialRepository.save(c);
            });
        return jwtPublicService.createToken(cred.getCliente().getId());
    }

    private String exchangeCodeForAccessToken(String code, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", properties.getGoogle().getClientId());
        body.add("client_secret", properties.getGoogle().getClientSecret());
        body.add("redirect_uri", redirectUri);
        body.add("grant_type", "authorization_code");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(GOOGLE_TOKEN_URL, request, Map.class);
        if (response.getBody() == null || !response.getBody().containsKey("access_token")) {
            throw new IllegalStateException("Google token exchange failed");
        }
        return (String) response.getBody().get("access_token");
    }

    private GoogleUserInfo fetchUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(
            GOOGLE_USERINFO_URL,
            org.springframework.http.HttpMethod.GET,
            request,
            GoogleUserInfo.class
        );
        if (response.getBody() == null) {
            throw new IllegalStateException("Google userinfo failed");
        }
        return response.getBody();
    }

    private Cliente findOrCreateCliente(GoogleUserInfo userInfo) {
        return credentialRepository.findByProviderAndExternalId(PROVIDER_GOOGLE, userInfo.getSub())
            .map(ClienteCredential::getCliente)
            .orElseGet(() -> {
                Cliente c = new Cliente();
                c.setNome(userInfo.getName() != null && !userInfo.getName().isBlank() ? userInfo.getName() : "Cliente");
                c.setEmail(userInfo.getEmail() != null && !userInfo.getEmail().isBlank() ? userInfo.getEmail() : userInfo.getSub() + "@google.placeholder");
                c.setTelefone(null);
                return clienteRepository.save(c);
            });
    }
}
