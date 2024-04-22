package in.koreatech.koin.fixture;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.shop.model.EventArticle;
import in.koreatech.koin.domain.shop.model.EventArticleImage;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.repository.EventArticleRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class EventArticleFixture {

    private final EventArticleRepository eventArticleRepository;

    public EventArticleFixture(
        EventArticleRepository eventArticleRepository
    ) {
        this.eventArticleRepository = eventArticleRepository;
    }

    public EventArticle 할인_이벤트(
        Shop shop,
        LocalDate startDate,
        LocalDate endDate
    ) {
        EventArticle eventArticle = eventArticleRepository.save(
            EventArticle.builder()
                .shop(shop)
                .title("할인 이벤트")
                .content("사장님이 미쳤어요!")
                .ip("")
                .startDate(startDate)
                .endDate(endDate)
                .hit(0)
                .build()
        );

        eventArticle.getThumbnailImages()
            .addAll(
                List.of(
                    EventArticleImage.builder()
                        .thumbnailImage("https://eventimage.com/할인_이벤트.jpg")
                        .eventArticle(eventArticle)
                        .build(),
                    EventArticleImage.builder()
                        .thumbnailImage("https://eventimage.com/할인_이벤트.jpg")
                        .eventArticle(eventArticle)
                        .build()
                )
            );
        return eventArticleRepository.save(eventArticle);
    }
}
