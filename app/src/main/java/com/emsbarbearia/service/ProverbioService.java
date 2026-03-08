package com.emsbarbearia.service;

import com.emsbarbearia.dto.ProverbioResponse;
import com.emsbarbearia.entity.Proverbio;
import com.emsbarbearia.repository.ProverbioRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProverbioService {

    private final ProverbioRepository proverbioRepository;

    public ProverbioService(ProverbioRepository proverbioRepository) {
        this.proverbioRepository = proverbioRepository;
    }

    public Optional<ProverbioResponse> getRandom() {
        return proverbioRepository.findRandom()
            .map(this::toResponse);
    }

    private ProverbioResponse toResponse(Proverbio p) {
        return new ProverbioResponse(p.getReferencia(), p.getTexto());
    }
}
