package in.koreatech.koin.domain.ownershop.dto;

import java.time.LocalTime;
import java.util.List;

import in.koreatech.koin.domain.shop.model.Shop;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OwnerShopsRequest(
    @Schema(description = "주소", example = "충청남도 천안시 동남구 병천면 충절로 1600")
    @NotBlank(message = "주소를 입력해주세요.")
    String address,

    @Schema(description = "상점 카테고리 고유 id 리스트", example = "[1]")
    List<Long> categoryIds,

    @Schema(description = "배달 가능 여부", example = "false")

    Boolean delivery,

    @Schema(description = "배달 금액", example = "1000")
    Long deliveryPrice,

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
    Boolean payBank,

    @Schema(description = "카드 가능 여부", example = "true")
    Boolean payCard,

    @Schema(description = "전화번호", example = "041-123-4567")
    @NotBlank(message = "전화번호를 입력해주세요.")
    String phone
) {
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
