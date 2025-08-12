package in.koreatech.koin.domain.order.shop.dto.shopsearch;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.shop.model.readmodel.OrderableShopBaseInfo;
import in.koreatech.koin.domain.order.shop.model.domain.OrderableShopOpenStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OrderableShopSearchResultResponse(
    @Schema(description = "주문 가능 상점 검색 입력 키워드", example = "치킨ㄱ")
    String searchKeyword,

    @Schema(description = "주문 가능 상점 검색 처리 키워드", example = "치킨")
    String processedSearchKeyword,

    @Schema(description = "주문 가능 상점 검색 결과 개수", example = "5")
    Integer resultCount,

    @Schema(description = "주문 가능 상점 검색 결과")
    List<OrderableShopSearchResult> searchResults
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record OrderableShopSearchResult(
        @Schema(description = "주문 가능 상점 식별자", example = "5")
        Integer orderableShopId,

        @Schema(description = "주문 가능 상점 이름", example = "5")
        String name,

        @Schema(description = "배달 가능 여부", example = "true")
        Boolean isDeliveryAvailable,

        @Schema(description = "포장 가능 여부", example = "true")
        Boolean isTakeoutAvailable,

        @Schema(description = "이벤트 여부", example = "true")
        Boolean serviceEvent,

        @Schema(description = "최소 주문 금액", example = "10000")
        Integer minimumOrderAmount,

        @Schema(description = "평균 리뷰 별점", example = "4.8")
        Double ratingAverage,

        @Schema(description = "리뷰 개수", example = "12")
        Long reviewCount,

        @Schema(description = "최소 배달비", example = "2500")
        Integer minimumDeliveryTip,

        @Schema(description = "최대 배달비", example = "4500")
        Integer maximumDeliveryTip,

        @Schema(description = "상점 현재 영업 여부", example = "true")
        Boolean isOpen,

        @Schema(description = "상점 썸네일 이미지 url", example = "https://static.koreatech.in/upload/market/test.jpg")
        String thumbnailImageUrl,

        @Schema(description = "상점 영업 상태", example = "OPERATING")
        OrderableShopOpenStatus openStatus,

        @Schema(description = "키워드 포함 메뉴 이름 목록")
        List<String> containMenuName
    ) {

        public static OrderableShopSearchResult from(
            OrderableShopBaseInfo info,
            String thumbnailImage,
            OrderableShopOpenStatus openStatus,
            List<String> containMenuNames
        ) {
            Integer orderableShopId = info.orderableShopId();
            return new OrderableShopSearchResult(
                orderableShopId,
                info.name(),
                info.isDeliveryAvailable(),
                info.isTakeoutAvailable(),
                info.serviceEvent(),
                info.minimumOrderAmount(),
                info.ratingAverage(),
                info.reviewCount(),
                info.minimumDeliveryTip(),
                info.maximumDeliveryTip(),
                info.isOpen(),
                thumbnailImage,
                openStatus,
                containMenuNames
            );
        }
    }

    public static OrderableShopSearchResultResponse empty(String searchKeyword, List<String> processedSearchKeywords) {
        return new OrderableShopSearchResultResponse(
            searchKeyword,
            String.join(" ", processedSearchKeywords),
            0,
            Collections.emptyList()
        );
    }

    public static OrderableShopSearchResultResponse from(
        String searchKeyword,
        List<String> processedSearchKeywords,
        List<OrderableShopSearchResult> searchResults
    ) {
        return new OrderableShopSearchResultResponse(
            searchKeyword,
            String.join(" ", processedSearchKeywords),
            searchResults.size(),
            searchResults
        );
    }
}
