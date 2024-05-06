package in.koreatech.koin.domain.coop.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
        LocalTime nowTime = now.toLocalTime();

        if (soldOutRequest.soldOut()) {
            dining.setSoldOut(now);
            LocalTime startTime = dining.getType().getStartTime();
            LocalTime endTime = dining.getType().getEndTime();
            if (diningSoldOutCacheRepository.findById(dining.getType().name()).isEmpty() &&
                (!nowTime.isBefore(startTime) && !nowTime.isAfter(endTime))) {
                eventPublisher.publishEvent(dining.getPlace());
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
