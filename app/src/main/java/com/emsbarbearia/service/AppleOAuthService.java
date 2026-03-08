package com.emsbarbearia.service;

import com.emsbarbearia.config.PublicAuthProperties;
import com.emsbarbearia.entity.Cliente;
import com.emsbarbearia.entity.ClienteCredential;
import com.emsbarbearia.repository.ClienteCredentialRepository;
import com.emsbarbearia.repository.ClienteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class AppleOAuthService {

    private static final String APPLE_AUTH_URL = "https://appleid.apple.com/auth/authorize";
    private static final String APPLE_TOKEN_URL = "https://appleid.apple.com/auth/token";
    private static final String PROVIDER_APPLE = "APPLE";
    private static final String SCOPE = "name email";

    private final PublicAuthProperties properties;
    private final ClienteRepository clienteRepository;
    private final ClienteCredentialRepository credentialRepository;
    private final JwtPublicService jwtPublicService;
    private final RestTemplate restTemplate = new RestTemplate();

    public AppleOAuthService(PublicAuthProperties properties,
                             ClienteRepository clienteRepository,
                             ClienteCredentialRepository credentialRepository,
                             JwtPublicService jwtPublicService) {
        this.properties = properties;
        this.clienteRepository = clienteRepository;
        this.credentialRepository = credentialRepository;
        this.jwtPublicService = jwtPublicService;
    }

    public String buildAuthorizationUrl(String redirectUri) {
        String clientId = properties.getApple().getClientId();
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("Apple OAuth client-id not configured");
        }
        String scope = URLEncoder.encode(SCOPE, StandardCharsets.UTF_8);
        String encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String state = UUID.randomUUID().toString();
        return APPLE_AUTH_URL + "?client_id=" + clientId + "&redirect_uri=" + encodedRedirect
            + "&response_type=code&response_mode=form_post&scope=" + scope + "&state=" + state;
    }

    public String exchangeCodeAndCreateToken(String code, String redirectUri) {
        String idToken = exchangeCodeForIdToken(code, redirectUri);
        String sub = decodeAppleSub(idToken);
        String email = decodeAppleEmail(idToken);
        Cliente cliente = findOrCreateCliente(sub, email);
        ClienteCredential cred = credentialRepository.findByProviderAndExternalId(PROVIDER_APPLE, sub)
            .orElseGet(() -> {
                ClienteCredential c = new ClienteCredential();
                c.setCliente(cliente);
                c.setProvider(PROVIDER_APPLE);
                c.setExternalId(sub);
                c.setPasswordHash(null);
                return credentialRepository.save(c);
            });
        return jwtPublicService.createToken(cred.getCliente().getId());
    }

    private String exchangeCodeForIdToken(String code, String redirectUri) {
        String clientSecret = buildAppleClientSecret();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", properties.getApple().getClientId());
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("grant_type", "authorization_code");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(APPLE_TOKEN_URL, request, Map.class);
        if (response.getBody() == null || !response.getBody().containsKey("id_token")) {
            throw new IllegalStateException("Apple token exchange failed");
        }
        return (String) response.getBody().get("id_token");
    }

    private String buildAppleClientSecret() {
        String teamId = properties.getApple().getTeamId();
        String keyId = properties.getApple().getKeyId();
        String privateKeyPem = properties.getApple().getPrivateKey();
        String clientId = properties.getApple().getClientId();
        if (teamId == null || keyId == null || privateKeyPem == null || clientId == null
            || teamId.isBlank() || keyId.isBlank() || privateKeyPem.isBlank() || clientId.isBlank()) {
            throw new IllegalStateException("Apple OAuth not fully configured");
        }
        try {
            PrivateKey key = parseP8PrivateKey(privateKeyPem);
            long now = System.currentTimeMillis() / 1000;
            return Jwts.builder()
                .header().keyId(keyId).and()
                .issuer(teamId)
                .subject(clientId)
                .audience().add("https://appleid.apple.com").and()
                .issuedAt(new Date((now - 60) * 1000))
                .expiration(new Date((now + 86400 * 180) * 1000))
                .signWith(key)
                .compact();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build Apple client secret", e);
        }
    }

    private PrivateKey parseP8PrivateKey(String pem) throws Exception {
        String content = pem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(content);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return kf.generatePrivate(spec);
    }

    @SuppressWarnings("unchecked")
    private String decodeAppleSub(String idToken) {
        try {
            String payload = new String(Base64.getUrlDecoder().decode(idToken.split("\\.")[1]), StandardCharsets.UTF_8);
            Map<String, Object> claims = new ObjectMapper().readValue(payload, Map.class);
            String sub = (String) claims.get("sub");
            if (sub == null || sub.isBlank()) {
                throw new IllegalStateException("Apple id_token missing sub");
            }
            return sub;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decode Apple id_token", e);
        }
    }

    @SuppressWarnings("unchecked")
    private String decodeAppleEmail(String idToken) {
        try {
            String payload = new String(Base64.getUrlDecoder().decode(idToken.split("\\.")[1]), StandardCharsets.UTF_8);
            Map<String, Object> claims = new ObjectMapper().readValue(payload, Map.class);
            return (String) claims.get("email");
        } catch (Exception e) {
            return null;
        }
    }

    private Cliente findOrCreateCliente(String sub, String email) {
        return credentialRepository.findByProviderAndExternalId(PROVIDER_APPLE, sub)
            .map(ClienteCredential::getCliente)
            .orElseGet(() -> {
                Cliente c = new Cliente();
                c.setNome("Cliente");
                c.setEmail(email != null && !email.isBlank() ? email : sub + "@apple.placeholder");
                c.setTelefone(null);
                return clienteRepository.save(c);
            });
    }
}
