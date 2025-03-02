package in.koreatech.koin.domain.shop.dto.menu.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import in.koreatech.koin.domain.shop.model.menu.Menu;
import in.koreatech.koin.domain.shop.model.menu.MenuOption;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin._common.validation.NotBlankElement;
import in.koreatech.koin._common.validation.SingleMenuPrice;
import in.koreatech.koin._common.validation.UniqueId;
import in.koreatech.koin._common.validation.UniqueUrl;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

@JsonNaming(value = SnakeCaseStrategy.class)
@SingleMenuPrice
public record CreateMenuRequest(
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
        """, description = "이미지 URL 리스트", requiredMode = REQUIRED)
    @Size(max = 3, message = "이미지는 최대 3개까지 입력 가능합니다.")
    @UniqueUrl(message = "이미지 URL은 중복될 수 없습니다.")
    @NotNull(message = "이미지 URL은 null일 수 없습니다.")
    @NotBlankElement(message = "빈 요소가 존재할 수 없습니다.")
    List<String> imageUrls,

    @Schema(example = "true", description = "단일 메뉴 여부", requiredMode = REQUIRED)
    @NotNull(message = "단일 메뉴 여부는 필수입니다.")
    boolean isSingle,

    @Schema(example = "짜장면", description = "메뉴명")
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

    public Menu toEntity(Shop shop) {
        return Menu.builder()
            .name(name)
            .shop(shop)
            .description(description)
            .build();
    }

    public List<MenuOption> toMenuOption(Menu menu) {
        if (isSingle) {
            return List.of(MenuOption.builder()
                    .menu(menu)
                    .option(name)
                    .price(singlePrice)
                    .build());
        }
        return optionPrices.stream().map(option -> option.toEntity(menu)).toList();
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerOptionPrice(
        @Schema(example = "대", description = "옵션명", requiredMode = REQUIRED)
        @NotNull @Size(min = 1, max = 50) String option,

        @Schema(example = "26000", description = "가격", requiredMode = REQUIRED)
        @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.")
        @NotNull Integer price
    ) {
        public MenuOption toEntity(Menu menu) {
            return MenuOption.builder()
                    .menu(menu)
                    .option(option)
                    .price(price)
                    .build();
        }
    }
}
