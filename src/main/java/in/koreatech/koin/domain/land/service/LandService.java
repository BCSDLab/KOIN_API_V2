package in.koreatech.koin.domain.land.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.json.JSONArray;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.land.dto.LandListItemResponse;
import in.koreatech.koin.domain.land.dto.LandResponse;
import in.koreatech.koin.domain.land.exception.LandNotFoundException;
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
            .orElseThrow(() -> LandNotFoundException.withDetail("id: " + id));

        String image = land.getImageUrls();
        List<String> imageUrls = null;

        if (image != null) {
            imageUrls = new JSONArray(image)
                .toList()
                .stream()
                .map(Object::toString)
                .toList();
        }

        return LandResponse.of(land, imageUrls, URLEncoder.encode(land.getInternalName(), StandardCharsets.UTF_8));
    }
}
