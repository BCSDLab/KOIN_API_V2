package in.koreatech.koin.admin.shop.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopOpen;
import in.koreatech.koin.global.validation.UniqueId;
import in.koreatech.koin.global.validation.UniqueUrl;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminModifyShopRequest(
    @Schema(description = "주소", example = "충청남도 천안시 동남구 병천면", requiredMode = NOT_REQUIRED)
    String address,

    @Schema(description = "상점 카테고리 고유 id 리스트", example = "[1, 2]", requiredMode = REQUIRED)
    @NotNull(message = "카테고리는 필수입니다.")
    @UniqueId(message = "카테고리 ID는 중복될 수 없습니다.")
    @Size(min = 1, message = "최소 한 개의 카테고리가 필요합니다.")
    List<Integer> categoryIds,

    @Schema(description = "배달 가능 여부", example = "true", requiredMode = REQUIRED)
    @NotNull(message = "배달 가능 여부는 필수입니다.")
    Boolean delivery,

    @Schema(description = "배달비", example = "1000", requiredMode = REQUIRED)
    @NotNull(message = "배달비는 필수입니다.")
    @PositiveOrZero(message = "배달비는 0원 이상이어야 합니다.")
    Integer deliveryPrice,

    @Schema(description = "설명", example = "string", requiredMode = NOT_REQUIRED)
    @NotNull(message = "상점 설명은 null일 수 없습니다.")
    String description,

    @Schema(description = "이미지 URL 리스트", example = """
        [ "https://static.koreatech.in/example.png" ]
        """, requiredMode = NOT_REQUIRED)
    @UniqueUrl(message = "이미지 URL은 중복될 수 없습니다.")
    List<String> imageUrls,

    @Schema(description = "이름", example = "수신반점", requiredMode = REQUIRED)
    @NotBlank(message = "이름은 필수입니다.")
    String name,

    @Schema(description = "요일별 휴무 여부 및 장사 시간", requiredMode = NOT_REQUIRED)
    List<InnerShopOpen> open,

    @Schema(description = "계좌 이체 가능 여부", example = "true", requiredMode = REQUIRED)
    @NotNull(message = "계좌 이체 가능 여부는 필수입니다.")
    Boolean payBank,

    @Schema(description = "카드 계산 가능 여부", example = "false", requiredMode = REQUIRED)
    @NotNull(message = "카드 계산 가능 여부는 필수입니다.")
    Boolean payCard,

    @Schema(description = "전화번호", example = "041-000-0000", requiredMode = NOT_REQUIRED)
    @Size(max = 50, message = "전화번호는 50자 이하로 입력해주세요.")
    String phone
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerShopOpen(
        @Schema(description = """
            요일 = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY']
            """, example = "MONDAY", requiredMode = REQUIRED)
        String dayOfWeek,

        @Schema(description = "휴무 여부", example = "false", requiredMode = REQUIRED)
        @NotNull(message = "휴무 여부는 필수입니다.")
        boolean closed,

        @JsonFormat(pattern = "HH:mm")
        @Schema(description = "오픈 시간", example = "02:00", requiredMode = NOT_REQUIRED)
        LocalTime openTime,

        @JsonFormat(pattern = "HH:mm")
        @Schema(description = "마감 시간", example = "16:00", requiredMode = NOT_REQUIRED)
        LocalTime closeTime
    ) {

        public ShopOpen toEntity(Shop shop) {
            return ShopOpen.builder()
                .shop(shop)
                .closed(closed)
                .openTime(openTime)
                .closeTime(closeTime)
                .dayOfWeek(dayOfWeek)
                .build();
        }
    }
}
