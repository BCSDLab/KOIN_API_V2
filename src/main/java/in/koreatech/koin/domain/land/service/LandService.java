package in.koreatech.koin.domain.land.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.land.dto.LandResponse;
import in.koreatech.koin.domain.land.repository.LandRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LandService {

    private final LandRepository landRepository;

    public List<LandResponse> getLands() {
        return landRepository.findAll()
            .stream()
            .map(LandResponse::from)
            .toList();
    }
}
