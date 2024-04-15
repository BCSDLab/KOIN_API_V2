package in.koreatech.koin.domain.shop.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopCategory;
import in.koreatech.koin.domain.shop.model.ShopImage;
import in.koreatech.koin.domain.shop.model.ShopOpen;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ShopResponse(
    @Schema(example = "충청남도 천안시 동남구 병천면", description = "주소")
    String address,

    @Schema(example = "true", description = "배달 가능 여부")
    Boolean delivery,

    @Schema(example = "1000", description = "배달비")
    Integer deliveryPrice,

    @Schema(example = "string", description = "설명")
    String description,

    @Schema(example = "1", description = "고유 id")
    Integer id,

    @Schema(description = "이미지 URL 리스트")
    List<String> imageUrls,

    @Schema(description = "상점에 있는 메뉴 카테고리 리스트")
    List<InnerMenuCategory> menuCategories,

    @Schema(example = "수신반점", description = "이름")
    String name,

    @Schema(description = "요일별 휴무 여부 및 장사 시간")
    List<InnerShopOpen> open,

    @Schema(example = "true", description = "계좌 이체 가능 여부")
    Boolean payBank,

    @Schema(example = "false", description = "카드 계산 가능 여부")
    Boolean payCard,

    @Schema(example = "041-000-0000", description = "전화번호")
    String phone,

    @Schema(description = "소속된 상점 카테고리 리스트")
    List<InnerShopCategory> shopCategories,

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(example = "2024-03-01", description = "업데이트 날짜")
    LocalDateTime updatedAt,

    @Schema(example = "true", description = "상점 이벤트 진행 여부")
    Boolean isEvent
) {

    public static ShopResponse from(Shop shop, Boolean isEvent) {
        return new ShopResponse(
            shop.getAddress(),
            shop.isDelivery(),
            shop.getDeliveryPrice(),
            shop.getDescription(),
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
                    shopOpen.getClosed(),
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
            isEvent
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerShopOpen(
        @Schema(example = "MONDAY", description = """
            요일 = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY']
            """)
        String dayOfWeek,

        @Schema(example = "false", description = "휴무 여부")
        Boolean closed,

        @Schema(example = "02:00", description = "오픈 시간")
        @JsonFormat(pattern = "HH:mm")
        LocalTime openTime,

        @Schema(example = "16:00", description = "마감 시간")
        @JsonFormat(pattern = "HH:mm")
        LocalTime closeTime
    ) {

        public static InnerShopOpen from(ShopOpen shopOpen) {
            return new InnerShopOpen(
                shopOpen.getDayOfWeek(),
                shopOpen.getClosed(),
                shopOpen.getOpenTime(),
                shopOpen.getCloseTime()
            );
        }
    }

    private record InnerShopCategory(
        @Schema(example = "1", description = "고유 id")
        Integer id,

        @Schema(example = "중국집", description = "이름")
        String name
    ) {

    }

    private record InnerMenuCategory(
        @Schema(example = "1", description = "고유 id")
        Integer id,

        @Schema(example = "대표 메뉴", description = "이름")
        String name
    ) {

    }
}
