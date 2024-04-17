package in.koreatech.koin.domain.shop.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopOpen;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ModifyShopRequest(
    @Schema(example = "충청남도 천안시 동남구 병천면", description = "주소", requiredMode = NOT_REQUIRED)
    String address,

    @Schema(example = "[1, 2]", description = "상점 카테고리 고유 id 리스트", requiredMode = REQUIRED)
    @NotNull(message = "카테고리는 필수입니다.")
    List<Integer> categoryIds,

    @Schema(example = "true", description = "배달 가능 여부", requiredMode = REQUIRED)
    @NotNull(message = "배달 가능 여부는 필수입니다.")
    Boolean delivery,

    @Schema(example = "1000", description = "배달비", requiredMode = REQUIRED)
    @NotNull(message = "배달비는 필수입니다.")
    Integer deliveryPrice,

    @Schema(example = "string", description = "설명", requiredMode = NOT_REQUIRED)
    @NotNull(message = "상점 설명은 null일 수 없습니다.")
    String description,

    @Schema(description = "이미지 URL 리스트", example = """
        [ "https://static.koreatech.in/example.png" ]
        """, requiredMode = NOT_REQUIRED)
    List<String> imageUrls,

    @Schema(example = "수신반점", description = "이름", requiredMode = REQUIRED)
    @NotBlank(message = "이름은 필수입니다.")
    String name,

    @Schema(description = "요일별 휴무 여부 및 장사 시간", requiredMode = NOT_REQUIRED)
    List<InnerShopOpen> open,

    @Schema(example = "true", description = "계좌 이체 가능 여부", requiredMode = REQUIRED)
    @NotNull(message = "계좌 이체 가능 여부는 필수입니다.")
    Boolean payBank,

    @Schema(example = "false", description = "카드 계산 가능 여부", requiredMode = REQUIRED)
    @NotNull(message = "카드 계산 가능 여부는 필수입니다.")
    Boolean payCard,

    @Schema(example = "041-000-0000", description = "전화번호", requiredMode = NOT_REQUIRED)
    @Size(max = 50, message = "전화번호는 50자 이하로 입력해주세요.")
    String phone
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerShopOpen(
        @Schema(example = "MONDAY", description = """
            요일 = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY']
            """, requiredMode = REQUIRED)
        String dayOfWeek,

        @Schema(example = "false", description = "휴무 여부", requiredMode = REQUIRED)
        @NotNull(message = "휴무 여부는 필수입니다.")
        Boolean closed,

        @JsonFormat(pattern = "HH:mm")
        @Schema(example = "02:00", description = "오픈 시간", requiredMode = NOT_REQUIRED)
        LocalTime openTime,

        @JsonFormat(pattern = "HH:mm")
        @Schema(example = "16:00", description = "마감 시간", requiredMode = NOT_REQUIRED)
        LocalTime closeTime
    ) {

        public ShopOpen toEntity(Shop shop) {
            return ShopOpen.builder()
                .shop(shop)
                .openTime(openTime)
                .closed(closed)
                .closeTime(closeTime)
                .dayOfWeek(dayOfWeek)
                .build();
        }
    }
}
