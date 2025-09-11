package in.koreatech.koin.domain.order.shop.dto.event;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.shop.model.readmodel.OrderableShopEvent;
import in.koreatech.koin.domain.shop.model.event.EventArticleImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    }

    public static OrderableShopEventsResponse of(
        List<OrderableShopEvent> orderableShopEvents,
        Map<Integer, List<EventArticleImage>> eventArticleImageMap) {

        List<InnerOrderableShopEventResponse> eventResponses = orderableShopEvents.stream()
            .map(event -> createEventResponse(event, eventArticleImageMap))
            .toList();

        return new OrderableShopEventsResponse(eventResponses);
    }

    private static InnerOrderableShopEventResponse createEventResponse(
        OrderableShopEvent event,
        Map<Integer, List<EventArticleImage>> eventArticleImageMap) {

        List<String> thumbnailImages = eventArticleImageMap.getOrDefault(event.eventId(), Collections.emptyList())
            .stream()
            .map(EventArticleImage::getThumbnailImage)
            .toList();

        return new InnerOrderableShopEventResponse(
            event.orderableShopId(),
            event.shopId(),
            event.shopName(),
            event.eventId(),
            event.title(),
            event.content(),
            thumbnailImages,
            event.startDate(),
            event.endDate()
        );
    }
}
