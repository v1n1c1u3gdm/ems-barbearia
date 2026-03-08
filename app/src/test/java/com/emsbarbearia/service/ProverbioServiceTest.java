package com.emsbarbearia.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.emsbarbearia.dto.ProverbioResponse;
import com.emsbarbearia.entity.Proverbio;
import com.emsbarbearia.repository.ProverbioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProverbioServiceTest {

    @Mock
    ProverbioRepository proverbioRepository;

    @InjectMocks
    ProverbioService proverbioService;

    @Test
    void getRandom_shouldReturnEmptyWhenNoProverbio() {
        when(proverbioRepository.findRandom()).thenReturn(Optional.empty());

        Optional<ProverbioResponse> result = proverbioService.getRandom();

        assertThat(result).isEmpty();
    }

    @Test
    void getRandom_shouldReturnResponseWhenProverbioFound() {
        Proverbio entity = new Proverbio();
        entity.setId(1L);
        entity.setReferencia("Proverbs 1:1");
        entity.setTexto("O temor ao SENHOR é o princípio do conhecimento.");
        when(proverbioRepository.findRandom()).thenReturn(Optional.of(entity));

        Optional<ProverbioResponse> result = proverbioService.getRandom();

        assertThat(result).isPresent();
        assertThat(result.get().referencia()).isEqualTo("Proverbs 1:1");
        assertThat(result.get().texto()).isEqualTo("O temor ao SENHOR é o princípio do conhecimento.");
    }
}
