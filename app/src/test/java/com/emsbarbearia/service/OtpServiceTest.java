package com.emsbarbearia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.emsbarbearia.dto.VerifyOtpRequest;
import com.emsbarbearia.entity.Cliente;
import com.emsbarbearia.entity.ClienteCredential;
import com.emsbarbearia.entity.OtpRequest;
import com.emsbarbearia.repository.ClienteCredentialRepository;
import com.emsbarbearia.repository.ClienteRepository;
import com.emsbarbearia.repository.OtpRequestRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    OtpRequestRepository otpRequestRepository;

    @Mock
    ClienteRepository clienteRepository;

    @Mock
    ClienteCredentialRepository credentialRepository;

    @Mock
    OtpSender otpSender;

    @Mock
    JwtPublicService jwtPublicService;

    @InjectMocks
    OtpService otpService;

    @Test
    void requestOtp_shouldSaveAndSendWhenNoRecentRequest() {
        when(otpRequestRepository.findFirstByPhoneOrderByCreatedAtDesc("+5511999999999"))
            .thenReturn(Optional.empty());
        when(otpRequestRepository.save(any(OtpRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        otpService.requestOtp("11999999999");

        verify(otpRequestRepository).save(any(OtpRequest.class));
        verify(otpSender).send(eq("+5511999999999"), any(String.class));
    }

    @Test
    void requestOtp_shouldThrow429WhenRequestWithinRateLimit() {
        OtpRequest recent = new OtpRequest();
        recent.setPhone("+5511999999999");
        recent.setCreatedAt(Instant.now());
        when(otpRequestRepository.findFirstByPhoneOrderByCreatedAtDesc("+5511999999999"))
            .thenReturn(Optional.of(recent));

        assertThatThrownBy(() -> otpService.requestOtp("11999999999"))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(429));
    }

    @Test
    void verifyOtp_shouldThrow401WhenCodeInvalid() {
        when(otpRequestRepository.findValidByPhoneAndCode(eq("+5511999999999"), eq("123456"), any(Instant.class)))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> otpService.verifyOtp(new VerifyOtpRequest("11999999999", "123456")))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(401));
    }

    @Test
    void verifyOtp_shouldReturnTokenWhenValidAndCredentialExists() {
        OtpRequest otp = new OtpRequest();
        otp.setPhone("+5511999999999");
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        ClienteCredential cred = new ClienteCredential();
        cred.setCliente(cliente);
        when(otpRequestRepository.findValidByPhoneAndCode(eq("+5511999999999"), eq("123456"), any(Instant.class)))
            .thenReturn(Optional.of(otp));
        when(credentialRepository.findByProviderAndExternalId("PHONE", "+5511999999999"))
            .thenReturn(Optional.of(cred));
        when(jwtPublicService.createToken(1L)).thenReturn("jwt-token");

        String token = otpService.verifyOtp(new VerifyOtpRequest("11999999999", "123456"));

        assertThat(token).isEqualTo("jwt-token");
        verify(otpRequestRepository).deleteByPhone("+5511999999999");
        verify(jwtPublicService).createToken(1L);
    }

    @Test
    void verifyOtp_shouldCreateClienteAndCredentialWhenNewPhoneThenReturnToken() {
        OtpRequest otp = new OtpRequest();
        otp.setPhone("+5511888888888");
        when(otpRequestRepository.findValidByPhoneAndCode(eq("+5511888888888"), eq("654321"), any(Instant.class)))
            .thenReturn(Optional.of(otp));
        when(credentialRepository.findByProviderAndExternalId("PHONE", "+5511888888888"))
            .thenReturn(Optional.empty());
        Cliente savedCliente = new Cliente();
        savedCliente.setId(2L);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            c.setId(2L);
            return c;
        });
        when(credentialRepository.save(any(ClienteCredential.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtPublicService.createToken(2L)).thenReturn("new-token");

        String token = otpService.verifyOtp(new VerifyOtpRequest("11888888888", "654321"));

        assertThat(token).isEqualTo("new-token");
        verify(clienteRepository).save(any(Cliente.class));
        verify(credentialRepository).save(any(ClienteCredential.class));
        verify(jwtPublicService).createToken(2L);
    }
}
