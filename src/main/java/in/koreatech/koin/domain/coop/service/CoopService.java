package in.koreatech.koin.domain.coop.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.coop.dto.DiningImageRequest;
import in.koreatech.koin.domain.coop.dto.SoldOutRequest;
import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.repository.DiningRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoopService {

    private final DiningRepository diningRepository;

    private final Clock clock;

    @Transactional
    public void changeSoldOut(SoldOutRequest soldOutRequest) {
        Dining dining = diningRepository.getById(soldOutRequest.menuId());
        if (soldOutRequest.soldOut() != null && soldOutRequest.soldOut()) {
            dining.setSoldOut(LocalDateTime.now(clock));
        }
    }

    @Transactional
    public void saveDiningImage(DiningImageRequest imageRequest) {
        Dining dining = diningRepository.getById(imageRequest.menuId());
        dining.setImageUrl(imageRequest.imageUrl());
    }
}
