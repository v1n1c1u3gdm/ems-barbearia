package com.emsbarbearia.service;

import com.emsbarbearia.dto.VerifyOtpRequest;
import com.emsbarbearia.entity.Cliente;
import com.emsbarbearia.entity.ClienteCredential;
import com.emsbarbearia.entity.OtpRequest;
import com.emsbarbearia.repository.ClienteCredentialRepository;
import com.emsbarbearia.repository.ClienteRepository;
import com.emsbarbearia.repository.OtpRequestRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OtpService {

    private static final String PROVIDER_PHONE = "PHONE";
    private static final int CODE_LENGTH = 6;
    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final Duration RATE_LIMIT = Duration.ofMinutes(1);

    private final OtpRequestRepository otpRequestRepository;
    private final ClienteRepository clienteRepository;
    private final ClienteCredentialRepository credentialRepository;
    private final OtpSender otpSender;
    private final JwtPublicService jwtPublicService;

    public OtpService(OtpRequestRepository otpRequestRepository, ClienteRepository clienteRepository,
                     ClienteCredentialRepository credentialRepository, OtpSender otpSender,
                     JwtPublicService jwtPublicService) {
        this.otpRequestRepository = otpRequestRepository;
        this.clienteRepository = clienteRepository;
        this.credentialRepository = credentialRepository;
        this.otpSender = otpSender;
        this.jwtPublicService = jwtPublicService;
    }

    @Transactional
    public void requestOtp(String phone) {
        String normalized = normalizePhone(phone);
        Optional<OtpRequest> last = otpRequestRepository.findFirstByPhoneOrderByCreatedAtDesc(normalized);
        if (last.isPresent() && last.get().getCreatedAt().isAfter(Instant.now().minus(RATE_LIMIT))) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Aguarde um minuto para solicitar novo código");
        }
        String code = generateCode();
        Instant expiresAt = Instant.now().plus(OTP_TTL);
        OtpRequest otp = new OtpRequest();
        otp.setPhone(normalized);
        otp.setCode(code);
        otp.setExpiresAt(expiresAt);
        otpRequestRepository.save(otp);
        otpSender.send(normalized, code);
    }

    @Transactional
    public String verifyOtp(VerifyOtpRequest request) {
        String phone = normalizePhone(request.telefone());
        OtpRequest otp = otpRequestRepository.findValidByPhoneAndCode(phone, request.code(), Instant.now())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Código inválido ou expirado"));
        otpRequestRepository.deleteByPhone(phone);
        Cliente cliente = findOrCreateCliente(phone);
        ClienteCredential cred = credentialRepository.findByProviderAndExternalId(PROVIDER_PHONE, phone)
            .orElseGet(() -> {
                ClienteCredential c = new ClienteCredential();
                c.setCliente(cliente);
                c.setProvider(PROVIDER_PHONE);
                c.setExternalId(phone);
                c.setPasswordHash(null);
                return credentialRepository.save(c);
            });
        return jwtPublicService.createToken(cred.getCliente().getId());
    }

    private static String normalizePhone(String phone) {
        if (phone == null) return "";
        String s = phone.replaceAll("\\s", "");
        return s.startsWith("+") ? s : "+55" + s;
    }

    private static String generateCode() {
        int n = ThreadLocalRandom.current().nextInt((int) Math.pow(10, CODE_LENGTH), (int) Math.pow(10, CODE_LENGTH + 1));
        return String.valueOf(n);
    }

    private Cliente findOrCreateCliente(String phone) {
        return credentialRepository.findByProviderAndExternalId(PROVIDER_PHONE, phone)
            .map(ClienteCredential::getCliente)
            .orElseGet(() -> {
                Cliente c = new Cliente();
                c.setNome("Cliente");
                c.setEmail(phone.replace("+", "") + "@phone.placeholder");
                c.setTelefone(phone);
                return clienteRepository.save(c);
            });
    }
}
