package in.koreatech.koin.domain.ownershop.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.event.EventArticle;
import in.koreatech.koin.domain.shop.model.event.EventArticleImage;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record OwnerShopEventsResponse(

    @Schema(description = "이벤트 목록", requiredMode = NOT_REQUIRED)
    List<InnerOwnerShopEventResponse> events
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerOwnerShopEventResponse(
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

        @Schema(description = "이벤트 이미지")
        List<String> thumbnailImages,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "시작일", example = "2024-10-22", requiredMode = REQUIRED)
        LocalDate startDate,

        @Schema(description = "종료일", example = "2024-10-25", requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate endDate
    ) {

        public static InnerOwnerShopEventResponse from(EventArticle eventArticle) {
            return new InnerOwnerShopEventResponse(
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

    public static OwnerShopEventsResponse from(Shop shop) {
        var innerShopEventResponses = shop.getEventArticles().stream()
            .sorted(Comparator.comparing(EventArticle::getUpdatedAt).reversed())
            .map(InnerOwnerShopEventResponse::from)
            .toList();
        return new OwnerShopEventsResponse(innerShopEventResponses);
    }
}
