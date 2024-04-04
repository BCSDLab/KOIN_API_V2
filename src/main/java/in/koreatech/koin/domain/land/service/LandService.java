package in.koreatech.koin.domain.land.service;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URLEncoder;
import java.util.List;

import org.json.JSONArray;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.land.dto.LandResponse;
import in.koreatech.koin.domain.land.dto.LandsGroupResponse;
import in.koreatech.koin.domain.land.dto.LandsResponse;
import in.koreatech.koin.domain.land.model.Land;
import in.koreatech.koin.domain.land.repository.LandRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LandService {

    private final LandRepository landRepository;

    public LandsGroupResponse getLands() {
        List<LandsResponse> lands = landRepository.findAll()
            .stream()
            .map(LandsResponse::from)
            .toList();

        return new LandsGroupResponse(lands);
    }

    public LandResponse getLand(Long id) {
        Land land = landRepository.getById(id);

        String image = land.getImageUrls();
        List<String> imageUrls = null;

        if (image != null) {
            imageUrls = new JSONArray(image)
                .toList()
                .stream()
                .map(Object::toString)
                .toList();
        }

        return LandResponse.of(land, imageUrls, URLEncoder.encode(land.getInternalName(), UTF_8));
    }
}
