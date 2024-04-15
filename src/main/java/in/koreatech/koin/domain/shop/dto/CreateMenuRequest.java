package in.koreatech.koin.domain.shop.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.Menu;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record CreateMenuRequest(
    @Schema(example = "[1, 2, 3]", description = "선택된 카테고리 고유 id 리스트")
    @NotNull List<Integer> categoryIds,

    @Schema(example = "저희 가게의 대표 메뉴 짜장면입니다.", description = "메뉴 구성 설명")
    @Size(max = 80) String description,

    @Schema(example = "[\"https://static.koreatech.in/example.png\"]", description = "이미지 URL 리스트")
    @Size(max = 3) List<String> imageUrls,

    @Schema(example = "true", description = "단일 메뉴 여부")
    @NotNull boolean isSingle,

    @Schema(example = "짜장면", description = "메뉴명")
    @NotNull @Size(min = 1, max = 25) String name,

    @Schema(description = "단일 메뉴가 아닐때의 옵션에 따른 가격 리스트 / 단일 메뉴일 경우 null")
    List<InnerOptionPrice> optionPrices,

    @Schema(description = "단일 메뉴일때의 가격 / 단일 메뉴가 아닐 경우 null")
    Integer singlePrice
) {

    public Menu toEntity(Integer shopId) {
        return Menu.builder()
            .name(name)
            .shopId(shopId)
            .description(description)
            .build();
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerOptionPrice(
        @Schema(example = "대", description = "옵션명")
        @NotNull @Size(min = 1, max = 50) String option,

        @Schema(example = "26000", description = "가격")
        @NotNull Integer price
    ) {

    }
}
