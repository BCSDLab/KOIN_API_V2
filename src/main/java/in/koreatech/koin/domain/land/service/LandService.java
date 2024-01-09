package in.koreatech.koin.domain.land.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.land.dto.LandListItemResponse;
import in.koreatech.koin.domain.land.dto.LandResponse;
import in.koreatech.koin.domain.land.model.Land;
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

    public LandResponse getLand(Long id) {
        Land land = landRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 복덕방입니다."));

        return LandResponse.from(land);
    }
}
