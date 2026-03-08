package com.emsbarbearia.service;

import com.emsbarbearia.dto.ClienteResponse;
import com.emsbarbearia.dto.PublicLoginRequest;
import com.emsbarbearia.dto.PublicRegisterRequest;
import com.emsbarbearia.entity.Cliente;
import com.emsbarbearia.entity.ClienteCredential;
import com.emsbarbearia.repository.ClienteCredentialRepository;
import com.emsbarbearia.repository.ClienteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PublicAuthService {

    public static final String PROVIDER_EMAIL = "EMAIL";

    private final ClienteRepository clienteRepository;
    private final ClienteCredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtPublicService jwtPublicService;

    public PublicAuthService(ClienteRepository clienteRepository,
                            ClienteCredentialRepository credentialRepository,
                            PasswordEncoder passwordEncoder,
                            JwtPublicService jwtPublicService) {
        this.clienteRepository = clienteRepository;
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtPublicService = jwtPublicService;
    }

    public String register(PublicRegisterRequest request) {
        if (credentialRepository.findByProviderAndExternalId(PROVIDER_EMAIL, request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }
        Cliente cliente = new Cliente();
        cliente.setNome(request.nome());
        cliente.setEmail(request.email());
        cliente.setTelefone(null);
        cliente = clienteRepository.save(cliente);

        ClienteCredential cred = new ClienteCredential();
        cred.setCliente(cliente);
        cred.setProvider(PROVIDER_EMAIL);
        cred.setExternalId(request.email());
        cred.setPasswordHash(passwordEncoder.encode(request.senha()));
        credentialRepository.save(cred);

        return jwtPublicService.createToken(cliente.getId());
    }

    public String login(PublicLoginRequest request) {
        ClienteCredential cred = credentialRepository.findByProviderAndExternalId(PROVIDER_EMAIL, request.email())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));
        if (!passwordEncoder.matches(request.senha(), cred.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }
        return jwtPublicService.createToken(cred.getCliente().getId());
    }

    public ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
            cliente.getId(),
            cliente.getNome(),
            cliente.getEmail(),
            cliente.getTelefone(),
            cliente.getCreatedAt()
        );
    }
}
