package in.koreatech.koin.domain.ownershop.service;

import in.koreatech.koin._common.event.EventArticleCreateShopEvent;
import in.koreatech.koin.domain.ownershop.dto.CreateEventRequest;
import in.koreatech.koin.domain.ownershop.dto.ModifyEventRequest;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopEventsResponse;
import in.koreatech.koin.domain.shop.cache.aop.RefreshShopsCache;
import in.koreatech.koin.domain.shop.model.event.EventArticle;
import in.koreatech.koin.domain.shop.model.event.EventArticleImage;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.repository.event.EventArticleRepository;
import jakarta.persistence.EntityManager;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerEventService {

    private final EntityManager entityManager;
    private final EventArticleRepository eventArticleRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final OwnerShopUtilService ownerShopUtilService;

    @Transactional
    @RefreshShopsCache
    public void createEvent(Integer ownerId, Integer shopId, CreateEventRequest createEventRequest) {
        Shop shop = ownerShopUtilService.getOwnerShopById(shopId, ownerId);
        EventArticle savedEventArticle = createEventArticle(createEventRequest, shop);
        List<EventArticleImage> savedThumbnailImages = savedEventArticle.getThumbnailImages();
        eventPublisher.publishEvent(new EventArticleCreateShopEvent(
                shop.getId(),
                shop.getName(),
                savedEventArticle.getTitle(),
                savedThumbnailImages.isEmpty() ? null : savedThumbnailImages.get(0).getThumbnailImage()
        ));
    }

    @Transactional
    @RefreshShopsCache
    public void modifyEvent(Integer ownerId, Integer shopId, Integer eventId, ModifyEventRequest modifyEventRequest) {
        ownerShopUtilService.getOwnerShopById(shopId, ownerId);
        EventArticle eventArticle = eventArticleRepository.getById(eventId);
        eventArticle.modifyArticle(
                modifyEventRequest.title(),
                modifyEventRequest.content(),
                modifyEventRequest.thumbnailImages(),
                modifyEventRequest.startDate(),
                modifyEventRequest.endDate(),
                entityManager
        );
    }

    @Transactional
    @RefreshShopsCache
    public void deleteEvent(Integer ownerId, Integer shopId, Integer eventId) {
        ownerShopUtilService.getOwnerShopById(shopId, ownerId);
        eventArticleRepository.deleteById(eventId);
    }

    public OwnerShopEventsResponse getShopEvent(Integer shopId, Integer ownerId) {
        Shop shop = ownerShopUtilService.getOwnerShopById(shopId, ownerId);
        return OwnerShopEventsResponse.from(shop);
    }

    private EventArticle createEventArticle(CreateEventRequest createEventRequest, Shop shop) {
        EventArticle eventArticle = EventArticle.builder()
                .shop(shop)
                .startDate(createEventRequest.startDate())
                .endDate(createEventRequest.endDate())
                .title(createEventRequest.title())
                .content(createEventRequest.content())
                .user(shop.getOwner().getUser())
                .hit(0)
                .ip("")
                .build();
        EventArticle savedEventArticle = eventArticleRepository.save(eventArticle);
        savedEventArticle.addThumbnailImages(createEventRequest.thumbnailImages());
        return savedEventArticle;
    }
}
