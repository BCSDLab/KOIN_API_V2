package in.koreatech.koin.domain.coop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.coop.dto.DiningImageRequest;
import in.koreatech.koin.domain.dining.repository.DiningRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoopService {

    private final DiningRepository diningRepository;

    @Transactional
    public void saveDiningImage(DiningImageRequest image) {
        diningRepository.updateDiningImage(image.menuId(), image.imageUrl());
    }
}
