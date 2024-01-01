package in.koreatech.koin.domain.land.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.land.dto.LandListItemResponse;
import in.koreatech.koin.domain.land.repository.LandRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LandService {

    private final LandRepository landRepository;

    public List<LandListItemResponse> getLands() {
        return landRepository.findAll()
            .stream()
            .map(LandListItemResponse::from)
            .toList();
    }
}
