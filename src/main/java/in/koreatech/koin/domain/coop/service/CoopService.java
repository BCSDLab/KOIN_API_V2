package in.koreatech.koin.domain.coop.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.coop.dto.DiningImageRequest;
import in.koreatech.koin.domain.coop.dto.SoldOutRequest;
import in.koreatech.koin.domain.coop.model.DiningSoldOutEvent;
import in.koreatech.koin.domain.coop.repository.DiningSoldOutCacheRepository;
import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.repository.DiningRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoopService {

    private final Clock clock;
    private final ApplicationEventPublisher eventPublisher;
    private final DiningRepository diningRepository;
    private final DiningSoldOutCacheRepository diningSoldOutCacheRepository;

    @Transactional
    public void changeSoldOut(SoldOutRequest soldOutRequest) {
        Dining dining = diningRepository.getById(soldOutRequest.menuId());
        LocalDateTime now = LocalDateTime.now(clock);

        if (soldOutRequest.soldOut()) {
            dining.setSoldOut(now);
            LocalDateTime startTime = dining.getType().getStartTime().atDate(now.toLocalDate());
            LocalDateTime endTime = dining.getType().getEndTime().atDate(now.toLocalDate());
            if (diningSoldOutCacheRepository.findById(dining.getPlace()).isEmpty() &&
                (!now.isBefore(startTime) && !now.isAfter(endTime))) {
                eventPublisher.publishEvent(new DiningSoldOutEvent(dining.getPlace(), dining.getType()));
            }
        } else {
            dining.cancelSoldOut();
        }
    }

    @Transactional
    public void saveDiningImage(DiningImageRequest imageRequest) {
        Dining dining = diningRepository.getById(imageRequest.menuId());
        dining.setImageUrl(imageRequest.imageUrl());
    }
}
