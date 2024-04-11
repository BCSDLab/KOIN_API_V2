package in.koreatech.koin.domain.shop.dto;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.EventArticle;
import in.koreatech.koin.domain.shop.model.EventArticleImage;
import in.koreatech.koin.domain.shop.model.Shop;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ShopEventsResponse(

    @Schema(description = "이벤트 목록")
    List<InnerShopEventResponse> events
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerShopEventResponse(
        @Schema(description = "상점 ID", example = "1")
        Integer shopId,

        @Schema(description = "상점 이름", example = "술꾼")
        String shopName,

        @Schema(description = "이벤트 ID", example = "1")
        Integer eventId,

        @Schema(description = "이벤트 제목", example = "콩순이 사장님이 미쳤어요!!")
        String title,

        @Schema(description = "이벤트 내용", example = "콩순이 가게 전메뉴 90% 할인! 가게 폐업 임박...")
        String content,

        @Schema(description = "이벤트 이미지")
        List<String> thumbnailImages,

        @Schema(description = "시작일", example = "2024-10-22")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate startDate,

        @Schema(description = "종료일", example = "2024-10-25")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate endDate
    ) {

        public static InnerShopEventResponse from(EventArticle eventArticle) {
            return new InnerShopEventResponse(
                eventArticle.getShop().getId(),
                eventArticle.getShop().getName(),
                eventArticle.getId(),
                eventArticle.getTitle(),
                eventArticle.getContent(),
                eventArticle.getThumbnailImages()
                    .stream().map(EventArticleImage::getThumbnailImage)
                    .toList(),
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
