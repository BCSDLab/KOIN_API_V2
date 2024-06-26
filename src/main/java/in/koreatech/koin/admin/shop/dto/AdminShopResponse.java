package in.koreatech.koin.admin.shop.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopCategory;
import in.koreatech.koin.domain.shop.model.ShopImage;
import in.koreatech.koin.domain.shop.model.ShopOpen;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminShopResponse(
    @Schema(description = "주소", example = "충청남도 천안시 동남구 병천면")
    String address,

    @Schema(description = "배달 가능 여부", example = "true")
    Boolean delivery,

    @Schema(description = "배달비", example = "1000", requiredMode = REQUIRED)
    Integer deliveryPrice,

    @Schema(description = "설명", example = "string")
    String description,

    @Schema(description = "고유 id", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "이미지 URL 리스트")
    List<String> imageUrls,

    @Schema(description = "상점에 있는 메뉴 카테고리 리스트")
    List<InnerMenuCategory> menuCategories,

    @Schema(description = "이름", example = "수신반점")
    String name,

    @Schema(description = "요일별 휴무 여부 및 장사 시간")
    List<InnerShopOpen> open,

    @Schema(description = "계좌 이체 가능 여부", example = "true", requiredMode = REQUIRED)
    Boolean payBank,

    @Schema(description = "카드 계산 가능 여부", example = "false", requiredMode = REQUIRED)
    Boolean payCard,

    @Schema(description = "전화번호", example = "041-000-0000", requiredMode = NOT_REQUIRED)
    String phone,

    @Schema(description = "소속된 상점 카테고리 리스트")
    List<InnerShopCategory> shopCategories,

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "업데이트 날짜", example = "2024-03-01", requiredMode = REQUIRED)
    LocalDateTime updatedAt,

    @Schema(description = "삭제 여부", example = "false", requiredMode = REQUIRED)
    Boolean isDeleted,

    @Schema(description = "상점 이벤트 진행 여부", example = "true", requiredMode = REQUIRED)
    Boolean isEvent
) {

    public static AdminShopResponse from(Shop shop, Boolean isEvent) {
        Collections.sort(shop.getMenuCategories());
        return new AdminShopResponse(
            shop.getAddress(),
            shop.isDelivery(),
            shop.getDeliveryPrice(),
            (shop.getDescription() == null || shop.getDescription().isBlank()) ? "-" : shop.getDescription(),
            shop.getId(),
            shop.getShopImages().stream()
                .map(ShopImage::getImageUrl)
                .toList(),
            shop.getMenuCategories().stream().map(menuCategory ->
                new InnerMenuCategory(
                    menuCategory.getId(),
                    menuCategory.getName()
                )
            ).toList(),
            shop.getName(),
            shop.getShopOpens().stream().map(shopOpen ->
                new InnerShopOpen(
                    shopOpen.getDayOfWeek(),
                    shopOpen.isClosed(),
                    shopOpen.getOpenTime(),
                    shopOpen.getCloseTime()
                )
            ).toList(),
            shop.isPayBank(),
            shop.isPayCard(),
            shop.getPhone(),
            shop.getShopCategories().stream().map(shopCategoryMap -> {
                ShopCategory shopCategory = shopCategoryMap.getShopCategory();
                return new InnerShopCategory(
                    shopCategory.getId(),
                    shopCategory.getName()
                );
            }).toList(),
            shop.getUpdatedAt(),
            shop.isDeleted(),
            isEvent
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerShopOpen(
        @Schema(description = """
            요일 = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY']
            """, example = "MONDAY", requiredMode = REQUIRED)
        String dayOfWeek,

        @Schema(description = "휴무 여부", example = "false", requiredMode = REQUIRED)
        Boolean closed,

        @JsonFormat(pattern = "HH:mm")
        @Schema(description = "오픈 시간", example = "02:00", requiredMode = NOT_REQUIRED)
        LocalTime openTime,

        @JsonFormat(pattern = "HH:mm")
        @Schema(description = "마감 시간", example = "16:00", requiredMode = NOT_REQUIRED)
        LocalTime closeTime
    ) {

        public static InnerShopOpen from(ShopOpen shopOpen) {
            return new InnerShopOpen(
                shopOpen.getDayOfWeek(),
                shopOpen.isClosed(),
                shopOpen.getOpenTime(),
                shopOpen.getCloseTime()
            );
        }
    }

    private record InnerShopCategory(
        @Schema(description = "고유 id", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "이름", example = "중국집", requiredMode = REQUIRED)
        String name
    ) {

    }

    private record InnerMenuCategory(
        @Schema(description = "고유 id", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "이름", example = "대표 메뉴", requiredMode = REQUIRED)
        String name
    ) {

    }
}
