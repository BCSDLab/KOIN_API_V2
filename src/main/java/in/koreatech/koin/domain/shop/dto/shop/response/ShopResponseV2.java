package in.koreatech.koin.domain.shop.dto.shop.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopCategory;
import in.koreatech.koin.domain.shop.model.shop.ShopImage;
import in.koreatech.koin.domain.shop.model.shop.ShopOpen;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ShopResponseV2(
    @Schema(example = "충청남도 천안시 동남구 병천면", description = "주소")
    String address,

    @Schema(example = "true", description = "배달 가능 여부")
    Boolean delivery,

    @Schema(example = "1000", description = "배달비", requiredMode = REQUIRED)
    Integer deliveryPrice,

    @Schema(example = "string", description = "설명")
    String description,

    @Schema(example = "1", description = "고유 id", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "이미지 URL 리스트")
    List<String> imageUrls,

    @Schema(description = "상점에 있는 메뉴 카테고리 리스트")
    List<InnerMenuCategory> menuCategories,

    @Schema(example = "수신반점", description = "이름")
    String name,

    @Schema(description = "요일별 휴무 여부 및 장사 시간")
    List<InnerShopOpen> open,

    @Schema(example = "true", description = "계좌 이체 가능 여부", requiredMode = REQUIRED)
    Boolean payBank,

    @Schema(example = "false", description = "카드 계산 가능 여부", requiredMode = REQUIRED)
    Boolean payCard,

    @Schema(example = "041-000-0000", description = "전화번호", requiredMode = NOT_REQUIRED)
    String phone,

    @Schema(description = "메인 카테고리 고유 id", example = "2")
    Integer mainCategoryId,

    @Schema(description = "소속된 상점 카테고리 리스트")
    List<InnerShopCategory> shopCategories,

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(example = "2024-03-01", description = "업데이트 날짜", requiredMode = REQUIRED)
    LocalDateTime updatedAt,

    @Schema(example = "true", description = "상점 이벤트 진행 여부", requiredMode = REQUIRED)
    Boolean isEvent,

    @Schema(example = "국민은행", description = "은행", requiredMode = NOT_REQUIRED)
    String bank,

    @Schema(example = "110-439-1234567", description = "계좌번호", requiredMode = NOT_REQUIRED)
    String accountNumber,

    @Schema(description = "상점 영업 시작 시간", example = "11:30")
    @JsonFormat(pattern = "HH:mm")
    LocalTime openTime,

    @Schema(description = "상점 영업 종료 시간", example = "20:30")
    @JsonFormat(pattern = "HH:mm")
    LocalTime closeTime
) {

    public static ShopResponseV2 from(Shop shop, LocalDate now) {
        Collections.sort(shop.getMenuCategories());
        List<ShopOpen> shopOpens = shop.getShopOpens();
        String today = LocalDate.now().getDayOfWeek().toString();

        Optional<ShopOpen> todayShopOpen = shopOpens.stream()
            .filter(shopOpen -> shopOpen.getDayOfWeek().equals(today))
            .findFirst();

        LocalTime openTime = todayShopOpen.map(ShopOpen::getOpenTime).orElse(null);
        LocalTime closeTime = todayShopOpen.map(ShopOpen::getCloseTime).orElse(null);

        return new ShopResponseV2(
            shop.getAddress(),
            shop.getDelivery(),
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
            shop.getShopMainCategory() != null ? shop.getShopMainCategory().getId() : null,
            shop.getShopCategories().stream().map(shopCategoryMap -> {
                ShopCategory shopCategory = shopCategoryMap.getShopCategory();
                return new InnerShopCategory(
                    shopCategory.getId(),
                    shopCategory.getName()
                );
            }).toList(),
            shop.getUpdatedAt(),
            shop.getEventArticles().stream().anyMatch(events -> events.isEventDuration(now)),
            shop.getBank(),
            shop.getAccountNumber(),
            openTime,
            closeTime
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerShopOpen(
        @Schema(example = "MONDAY", description = """
            요일 = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY']
            """, requiredMode = REQUIRED)
        String dayOfWeek,

        @Schema(example = "false", description = "휴무 여부", requiredMode = REQUIRED)
        Boolean closed,

        @JsonFormat(pattern = "HH:mm")
        @Schema(example = "02:00", description = "오픈 시간", requiredMode = NOT_REQUIRED)
        LocalTime openTime,

        @JsonFormat(pattern = "HH:mm")
        @Schema(example = "16:00", description = "마감 시간", requiredMode = NOT_REQUIRED)
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
        @Schema(example = "1", description = "고유 id", requiredMode = REQUIRED)
        Integer id,

        @Schema(example = "중국집", description = "이름", requiredMode = REQUIRED)
        String name
    ) {

    }

    private record InnerMenuCategory(
        @Schema(example = "1", description = "고유 id", requiredMode = REQUIRED)
        Integer id,

        @Schema(example = "대표 메뉴", description = "이름", requiredMode = REQUIRED)
        String name
    ) {

    }
}
