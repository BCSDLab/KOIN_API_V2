package in.koreatech.koin.domain.ownershop.service;

import in.koreatech.koin.domain.ownershop.EventArticleCreateShopEvent;
import in.koreatech.koin.domain.ownershop.dto.CreateEventRequest;
import in.koreatech.koin.domain.ownershop.dto.ModifyEventRequest;
import in.koreatech.koin.domain.ownershop.dto.OwnerShopEventsResponse;
import in.koreatech.koin.domain.shop.model.article.EventArticle;
import in.koreatech.koin.domain.shop.model.article.EventArticleImage;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.repository.event.EventArticleRepository;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerEventService {

    private final EntityManager entityManager;
    private final ShopRepository shopRepository;
    private final EventArticleRepository eventArticleRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createEvent(Integer ownerId, Integer shopId, CreateEventRequest shopEventRequest) {
        Shop shop = getOwnerShopById(shopId, ownerId);
        EventArticle eventArticle = EventArticle.builder()
            .shop(shop)
            .startDate(shopEventRequest.startDate())
            .endDate(shopEventRequest.endDate())
            .title(shopEventRequest.title())
            .content(shopEventRequest.content())
            .user(shop.getOwner().getUser())
            .hit(0)
            .ip("")
            .build();
        EventArticle savedEventArticle = eventArticleRepository.save(eventArticle);
        for (String image : shopEventRequest.thumbnailImages()) {
            savedEventArticle.getThumbnailImages()
                .add(EventArticleImage.builder()
                    .eventArticle(eventArticle)
                    .thumbnailImage(image)
                    .build());
        }
        List<EventArticleImage> eventArticleImages = savedEventArticle.getThumbnailImages();
        eventPublisher.publishEvent(
            new EventArticleCreateShopEvent(
                shop.getId(),
                shop.getName(),
                savedEventArticle.getTitle(),
                eventArticleImages.isEmpty() ? null : eventArticleImages.get(0).getThumbnailImage()
            )
        );
    }

    @Transactional
    public void modifyEvent(Integer ownerId, Integer shopId, Integer eventId, ModifyEventRequest modifyEventRequest) {
        getOwnerShopById(shopId, ownerId);
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
    public void deleteEvent(Integer ownerId, Integer shopId, Integer eventId) {
        getOwnerShopById(shopId, ownerId);
        eventArticleRepository.deleteById(eventId);
    }

    public OwnerShopEventsResponse getShopEvent(Integer shopId, Integer ownerId) {
        Shop shop = getOwnerShopById(shopId, ownerId);
        return OwnerShopEventsResponse.from(shop);
    }

    private Shop getOwnerShopById(Integer shopId, Integer ownerId) {
        Shop shop = shopRepository.getById(shopId);
        if (!Objects.equals(shop.getOwner().getId(), ownerId)) {
            throw AuthorizationException.withDetail("ownerId: " + ownerId);
        }
        return shop;
    }
}
