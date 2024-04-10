package in.koreatech.koin.domain.ownershop.dto;

import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.shop.model.Shop;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record OwnerShopsRequest(
    @Schema(description = "주소", example = "충청남도 천안시 동남구 병천면 충절로 1600")
    @NotBlank(message = "주소를 입력해주세요.")
    String address,

    @Schema(description = "상점 카테고리 고유 id 리스트", example = "[1]")
    @Size(min = 1, message = "최소 한 개의 카테고리가 필요합니다.")
    List<Integer> categoryIds,

    @Schema(description = "배달 가능 여부", example = "false")
    @NotNull(message = "배달 가능 여부를 입력해주세요.")
    Boolean delivery,

    @Schema(description = "배달 금액", example = "1000")
    @NotNull(message = "배달 금액을 입력해주세요.")
    @Min(value = 0, message = "배달 금액은 0원 이상이어야 합니다.")
    Integer deliveryPrice,

    @Schema(description = "기타정보", example = "이번주 전 메뉴 10% 할인 이벤트합니다.")
    String description,

    @Schema(description = "이미지 URL 리스트", example = "[\"string\"]")
    List<String> imageUrls,

    @Schema(description = "가게명", example = "써니 숯불 도시락")
    @NotBlank(message = "상점 이름을 입력해주세요.")
    String name,

    @Schema(description = "요일별 운영 시간과 휴무 여부")
    List<InnerOpenRequest> open,

    @Schema(description = "계좌 이체 가능 여부", example = "true")
    @NotNull(message = "계좌 이체 가능 여부를 입력해주세요.")
    Boolean payBank,

    @Schema(description = "카드 가능 여부", example = "true")
    @NotNull(message = "카드 가능 여부를 입력해주세요.")
    Boolean payCard,

    @Schema(description = "전화번호", example = "041-123-4567")
    @NotBlank(message = "전화번호를 입력해주세요.")
    @Pattern(regexp = "^[0-9]{3}-[0-9]{3,4}-[0-9]{4}", message = "전화번호 형식이 올바르지 않습니다.")
    String phone
) {

    public Shop toEntity(Owner owner) {
        return Shop.builder()
            .owner(owner)
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

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerOpenRequest(
        @Schema(description = "닫는 시간", example = "22:30")
        LocalTime closeTime,

        @Schema(description = "휴무 여부", example = "false")
        Boolean closed,

        @Schema(description = "요일", example = "MONDAY")
        @NotBlank(message = "영업 요일을 입력해주세요.")
        String dayOfWeek,

        @Schema(description = "여는 시간", example = "10:00")
        LocalTime openTime
    ) {

    }
}
