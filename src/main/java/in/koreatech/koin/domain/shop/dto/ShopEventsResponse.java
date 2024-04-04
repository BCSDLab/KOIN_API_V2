package in.koreatech.koin.domain.shop.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.EventArticle;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ShopEventsResponse(

    @Schema(description = "이벤트 목록")
    List<InnerShopEventResponse> events
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerShopEventResponse(
        @Schema(description = "이벤트 제목", example = "콩순이 사장님이 미쳤어요!!")
        String title,

        @Schema(description = "이벤트 내용", example = "콩순이 가게 전메뉴 90% 할인! 가게 폐업 임박...")
        String content,

        @Schema(description = "이벤트 이미지", example = "https://example-static.koreatech.in/route/img.png")
        String thumbnailImage

    ) {

        public static InnerShopEventResponse from(EventArticle eventArticle) {
            return new InnerShopEventResponse(
                eventArticle.getTitle(),
                eventArticle.getContent(),
                eventArticle.getThumbnail()
            );
        }
    }

    public static ShopEventsResponse from(List<EventArticle> eventArticles) {
        var eventResponses = eventArticles.stream()
            .map(InnerShopEventResponse::from)
            .toList();
        return new ShopEventsResponse(
            eventResponses
        );
    }
}
