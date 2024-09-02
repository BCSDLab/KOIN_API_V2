package in.koreatech.koin.domain.shop.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.global.validation.NotBlankElement;
import in.koreatech.koin.global.validation.SingleMenuPrice;
import in.koreatech.koin.global.validation.UniqueId;
import in.koreatech.koin.global.validation.UniqueUrl;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
@SingleMenuPrice
public record ModifyMenuRequest(
    @Schema(example = "[1, 2, 3]", description = "선택된 카테고리 고유 id 리스트", requiredMode = REQUIRED)
    @NotNull(message = "카테고리는 필수입니다.")
    @Size(min = 1, message = "최소 한 개의 카테고리가 필요합니다.")
    @UniqueId(message = "카테고리 ID는 중복될 수 없습니다.")
    List<Integer> categoryIds,

    @Schema(example = "저희 가게의 대표 메뉴 짜장면입니다.", description = "메뉴 구성 설명", requiredMode = REQUIRED)
    @Size(max = 80, message = "메뉴 구성 설명은 80자 이하로 입력해주세요.")
    String description,

    @Schema(example = """
        [ "https://static.koreatech.in/example.png" ]
        """, description = "이미지 URL 리스트", requiredMode = NOT_REQUIRED)
    @Size(max = 3, message = "이미지는 최대 3개까지 입력 가능합니다.")
    @UniqueUrl(message = "이미지 URL은 중복될 수 없습니다.")
    @NotBlankElement(message = "이미지 URL은 필수입니다.")
    List<String> imageUrls,

    @Schema(example = "true", description = "단일 메뉴 여부", requiredMode = REQUIRED)
    @NotNull(message = "단일 메뉴 여부는 필수입니다.")
    boolean isSingle,

    @Schema(example = "짜장면", description = "메뉴명", requiredMode = REQUIRED)
    @NotNull(message = "메뉴명은 필수입니다.")
    @Size(min = 1, max = 25, message = "메뉴명은 1자 이상 25자 이하로 입력해주세요.")
    String name,

    @Schema(description = "단일 메뉴가 아닐때의 옵션에 따른 가격 리스트 / 단일 메뉴일 경우 null", requiredMode = NOT_REQUIRED)
    @Valid
    List<InnerOptionPrice> optionPrices,

    @Schema(description = "단일 메뉴일때의 가격 / 단일 메뉴가 아닐 경우 null", requiredMode = NOT_REQUIRED)
    @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.")
    Integer singlePrice
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerOptionPrice(
        @Schema(example = "대", description = "옵션명", requiredMode = REQUIRED)
        @NotNull(message = "옵션명은 필수입니다.")
        @Size(min = 1, max = 50, message = "옵션명은 1자 이상 50자 이하로 입력해주세요.")
        String option,

        @Schema(example = "26000", description = "가격", requiredMode = REQUIRED)
        @NotNull(message = "가격은 필수입니다.")
        @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.")
        Integer price
    ) {

    }
}
