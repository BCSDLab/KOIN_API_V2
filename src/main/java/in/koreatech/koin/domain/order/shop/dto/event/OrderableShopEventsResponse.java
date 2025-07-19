package in.koreatech.koin.domain.order.shop.dto.event;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.shop.model.event.EventArticle;
import in.koreatech.koin.domain.shop.model.event.EventArticleImage;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OrderableShopEventsResponse(

    @Schema(description = "상점 이벤트 목록")
    List<InnerOrderableShopEventResponse> shopEvents
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerOrderableShopEventResponse(
        @Schema(description = "주문 가능 상점 ID", example = "1", requiredMode = REQUIRED)
        Integer orderableShopId,

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

        public static InnerOrderableShopEventResponse from(EventArticle eventArticle, Integer orderableShopId) {
            return new InnerOrderableShopEventResponse(
                orderableShopId,
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

    public static OrderableShopEventsResponse of(OrderableShop orderableShop, Clock clock) {
        List<InnerOrderableShopEventResponse> eventResponses = toEventResponses(orderableShop, clock).toList();
        return new OrderableShopEventsResponse(eventResponses);
    }

    public static OrderableShopEventsResponse of(List<OrderableShop> orderableShops, Clock clock) {
        List<InnerOrderableShopEventResponse> eventResponses = orderableShops.stream()
            .flatMap(orderableShop -> toEventResponses(orderableShop, clock))
            .toList();

        return new OrderableShopEventsResponse(eventResponses);
    }

    private static Stream<InnerOrderableShopEventResponse> toEventResponses(OrderableShop shop, Clock clock) {
        return shop.getOngoingEvent(clock).stream()
            .map(eventArticle -> InnerOrderableShopEventResponse.from(eventArticle, shop.getId()));
    }
}
