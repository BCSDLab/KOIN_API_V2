package in.koreatech.koin.domain.ownershop.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import in.koreatech.koin.domain.shop.model.shop.ShopOpen;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopCategory;
import in.koreatech.koin.global.validation.NotBlankElement;
import in.koreatech.koin.global.validation.UniqueId;
import in.koreatech.koin.global.validation.UniqueUrl;
import in.koreatech.koin.global.validation.DayOfWeek;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record OwnerShopsRequest(
        @Schema(description = "주소", example = "충청남도 천안시 동남구 병천면 충절로 1600", requiredMode = REQUIRED)
        @NotBlank(message = "주소를 입력해주세요.")
        String address,

        @Schema(description = "메인 카테고리 고유 id", example = "2", requiredMode = REQUIRED)
        @NotNull(message = "메인 카테고리는 필수입니다.")
        Integer mainCategoryId,

        @Schema(description = "상점 카테고리 고유 id 리스트", example = "[1]", requiredMode = REQUIRED)
        @NotNull(message = "카테고리를 입력해주세요.")
        @Size(min = 1, message = "최소 한 개의 카테고리가 필요합니다.")
        @UniqueId(message = "카테고리 ID는 중복될 수 없습니다.")
        List<Integer> categoryIds,

        @Schema(description = "배달 가능 여부", example = "false", requiredMode = REQUIRED)
        @NotNull(message = "배달 가능 여부를 입력해주세요.")
        Boolean delivery,

        @Schema(description = "배달 금액", example = "1000", requiredMode = REQUIRED)
        @NotNull(message = "배달 금액을 입력해주세요.")
        @PositiveOrZero(message = "배달비는 0원 이상이어야 합니다.")
        Integer deliveryPrice,

        @Schema(description = "기타정보", example = "이번주 전 메뉴 10% 할인 이벤트합니다.", requiredMode = REQUIRED)
        @NotNull(message = "상점 설명은 null일 수 없습니다.")
        String description,

        @Schema(description = "이미지 URL 리스트", example = """
                [ "https://testimage.com" ]
                """, requiredMode = REQUIRED)
        @UniqueUrl(message = "이미지 URL은 중복될 수 없습니다.")
        @NotNull(message = "이미지 URL은 null일 수 없습니다.")
        @NotBlankElement(message = "빈 요소가 존재할 수 없습니다.")
        List<String> imageUrls,

        @Schema(description = "가게명", example = "써니 숯불 도시락", requiredMode = REQUIRED)
        @NotBlank(message = "상점 이름을 입력해주세요.")
        String name,

        @Schema(description = "요일별 운영 시간과 휴무 여부", requiredMode = REQUIRED)
        @Size(min = 7, max = 7, message = "요일별 운영 시간은 7개여야 합니다.")
        @NotNull
        @Valid
        List<InnerOpenRequest> open,

        @Schema(description = "계좌 이체 가능 여부", example = "true", requiredMode = REQUIRED)
        @NotNull(message = "계좌 이체 가능 여부를 입력해주세요.")
        Boolean payBank,

        @Schema(description = "카드 가능 여부", example = "true", requiredMode = REQUIRED)
        @NotNull(message = "카드 가능 여부를 입력해주세요.")
        Boolean payCard,

        @Schema(description = "전화번호", example = "041-123-4567", requiredMode = REQUIRED)
        @NotBlank(message = "전화번호를 입력해주세요.")
        @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}", message = "전화번호 형식이 올바르지 않습니다.")
        String phone
) {

    public Shop toEntity(Owner owner, ShopCategory shopMainCategory) {
        return Shop.builder()
                .owner(owner)
                .shopMainCategory(shopMainCategory)
                .address(address)
                .deliveryPrice(deliveryPrice)
                .delivery(delivery)
                .description(description)
                .payBank(payBank)
                .payCard(payCard)
                .phone(phone)
                .name(name)
                .internalName(name)
                .chosung(name.substring(0, 1))
                .isDeleted(false)
                .isEvent(false)
                .remarks("")
                .hit(0)
                .build();
    }

    public List<ShopOpen> toShopOpens(Shop shop) {
        return open.stream().map(open -> open.toEntity(shop)).toList();
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerOpenRequest(
            @Schema(description = "닫는 시간", example = "22:30", requiredMode = REQUIRED)
            LocalTime closeTime,

            @Schema(description = "휴무 여부", example = "false", requiredMode = REQUIRED)
            Boolean closed,

            @Schema(description = "요일", example = "MONDAY", requiredMode = REQUIRED)
            @NotBlank(message = "영업 요일을 입력해주세요.")
            @DayOfWeek
            String dayOfWeek,

            @Schema(description = "여는 시간", example = "10:00", requiredMode = REQUIRED)
            LocalTime openTime
    ) {
        public ShopOpen toEntity(Shop shop) {
            return ShopOpen.builder()
                    .shop(shop)
                    .openTime(openTime)
                    .closeTime(closeTime)
                    .dayOfWeek(dayOfWeek)
                    .closed(closed)
                    .build();
        }
    }
}
