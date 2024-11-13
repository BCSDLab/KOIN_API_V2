package in.koreatech.koin.domain.shop.dto.shop;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.article.EventArticle;
import in.koreatech.koin.domain.shop.model.article.EventArticleImage;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ShopEventsResponse(

    @Schema(description = "이벤트 목록")
    List<InnerShopEventResponse> events
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerShopEventResponse(
        @Schema(description = "상점 ID", example = "1", requiredMode = REQUIRED)
        Integer shopId,

        @Schema(description = "상점 이름", example = "술꾼", requiredMode = REQUIRED)
        String shopName,

        @Schema(description = "이벤트 ID", example = "1", requiredMode = REQUIRED)
        Integer eventId,

        @Schema(description = "이벤트 제목", example = "콩순이 사장님이 미쳤어요!!", requiredMode = REQUIRED)
        String title,

        @Schema(description = "이벤트 내용", example = "콩순이 가게 전메뉴 90% 할인! 가게 폐업 임박...", requiredMode = REQUIRED)
        String content,

        @Schema(description = "이벤트 이미지", example = """
            [ "https://static.koreatech.in/example.png" ]
            """, requiredMode = NOT_REQUIRED)
        List<String> thumbnailImages,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "시작일", example = "2024-10-22", requiredMode = REQUIRED)
        LocalDate startDate,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "종료일", example = "2024-10-25", requiredMode = REQUIRED)
        LocalDate endDate
    ) {

        public static InnerShopEventResponse from(EventArticle eventArticle) {
            return new InnerShopEventResponse(
                eventArticle.getShop().getId(),
                eventArticle.getShop().getName(),
                eventArticle.getId(),
                eventArticle.getTitle(),
                eventArticle.getContent(),
                Optional.ofNullable(eventArticle.getShop())
                    .map(Shop::getShopMainCategory)
                    .map(ShopCategory::getEventImageUrl)
                    .map(List::of)
                    .orElse(null),
                eventArticle.getStartDate(),
                eventArticle.getEndDate()
            );
        }
    }

    public static ShopEventsResponse of(List<Shop> shops, Clock clock) {
        List<InnerShopEventResponse> innerShopEventResponses = new ArrayList<>();
        for (Shop shop : shops) {
            for (EventArticle eventArticle : shop.getEventArticles()) {
                if (!eventArticle.getStartDate().isAfter(LocalDate.now(clock)) &&
                    !eventArticle.getEndDate().isBefore(LocalDate.now(clock))) {
                    innerShopEventResponses.add(InnerShopEventResponse.from(eventArticle));
                }
            }
        }
        return new ShopEventsResponse(innerShopEventResponses);
    }

    public static ShopEventsResponse of(Shop shop, Clock clock) {
        List<InnerShopEventResponse> innerShopEventResponses = new ArrayList<>();
        for (EventArticle eventArticle : shop.getEventArticles()) {
            if (!eventArticle.getStartDate().isAfter(LocalDate.now(clock)) &&
                !eventArticle.getEndDate().isBefore(LocalDate.now(clock))) {
                innerShopEventResponses.add(InnerShopEventResponse.from(eventArticle));
            }
        }
        return new ShopEventsResponse(innerShopEventResponses);
    }
}
