package in.koreatech.koin.unit.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.order.cart.dto.CartAddItemCommand.Option;
import in.koreatech.koin.domain.order.cart.model.OrderableShopMenuOptions;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOptionGroup;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.unit.fixture.OrderableShopFixture;
import in.koreatech.koin.unit.fixture.OrderableShopMenuFixture;

class OrderableShopMenuOptionsTest {

    private OrderableShop orderableShop;
    private OrderableShopMenuOptionGroup requiredOptionGroup1;
    private OrderableShopMenuOptionGroup requiredOptionGroup2;
    private OrderableShopMenuOptionGroup optionalOptionGroup;
    private OrderableShopMenuOption requiredOption1;
    private OrderableShopMenuOption requiredOption2;
    private OrderableShopMenuOption requiredOption3;
    private OrderableShopMenuOption requiredOption4;
    private OrderableShopMenuOption requiredOption5;

    private OrderableShopMenuOption optionalOption1;
    private OrderableShopMenuOption optionalOption2;
    private List<OrderableShopMenuOption> menuOptions;

    @BeforeEach
    void setUp() {
        orderableShop = OrderableShopFixture.김밥천국(101);

        // 필수 옵션 그룹 1 (최소 1개, 최대 1개 선택)
        requiredOptionGroup1 = OrderableShopMenuFixture.createMenuOptionGroupWithEmptyMenuOption(
                orderableShop, "사이즈", 1, 1, true, 1);

        requiredOption1 = OrderableShopMenuFixture.createMenuOption(
                requiredOptionGroup1, "소", 0, 1);
        requiredOption2 = OrderableShopMenuFixture.createMenuOption(
                requiredOptionGroup1, "대", 1000, 2);

        // 필수 옵션 그룹 2 (최소 2개, 최대 3개 선택)
        requiredOptionGroup2 = OrderableShopMenuFixture.createMenuOptionGroupWithEmptyMenuOption(
                orderableShop, "국물", 2, 3, true, 2);

        requiredOption3 = OrderableShopMenuFixture.createMenuOption(
                requiredOptionGroup2, "라면", 2000, 3);
        requiredOption4 = OrderableShopMenuFixture.createMenuOption(
                requiredOptionGroup2, "우동", 1500, 4);
        requiredOption5 = OrderableShopMenuFixture.createMenuOption(
                requiredOptionGroup2, "떡국", 2500, 5);


        // 선택 옵션 그룹
        optionalOptionGroup = OrderableShopMenuFixture.createMenuOptionGroupWithEmptyMenuOption(
                orderableShop, "토핑", 0, 2, false, 3);

        optionalOption1 = OrderableShopMenuFixture.createMenuOption(
                optionalOptionGroup, "치즈", 500, 6);
        optionalOption2 = OrderableShopMenuFixture.createMenuOption(
                optionalOptionGroup, "단무지", 300, 7);

        menuOptions = List.of(
                requiredOption1, requiredOption2,
                requiredOption3, requiredOption4,
                requiredOption5,
                optionalOption1, optionalOption2
        );
    }

    private OrderableShopMenuOptions createOrderableShopMenuOptions() {
        return new OrderableShopMenuOptions(menuOptions);
    }

    @Nested
    @DisplayName("옵션선택이 없는 경우 테스트")
    class ResolveMenuOptionsWithNoOptionsTest {
        @Test
        void 옵션_선택이_NULL이고_필수그룹이_없으면_빈_리스트를_반환한다() {
            // given
            OrderableShopMenuOptions options = new OrderableShopMenuOptions(List.of(optionalOption1, optionalOption2));

            // when
            List<OrderableShopMenuOption> result = options.resolveSelectedOptions(null);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void 옵션_선택이_빈리스트이고_필수그룹이_없으면_빈_리스트를_반환한다() {
            // given
            OrderableShopMenuOptions options = new OrderableShopMenuOptions(List.of(optionalOption1, optionalOption2));

            // when
            List<OrderableShopMenuOption> result = options.resolveSelectedOptions(List.of());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void 옵션_선택이_NULL이고_필수그룹이_있으면_예외가_발생한다() {
            // given
            OrderableShopMenuOptions options = createOrderableShopMenuOptions();

            // when & then
            assertEquals(ApiResponseCode.REQUIRED_OPTION_GROUP_MISSING,
                    assertThrows(CustomException.class, () -> options.resolveSelectedOptions(null)).getErrorCode());
        }

        @Test
        void 옵션_선택이_빈리스트이고_필수그룹이_있으면_예외가_발생한다() {
            // given
            OrderableShopMenuOptions options = createOrderableShopMenuOptions();

            // when & then
            assertEquals(ApiResponseCode.REQUIRED_OPTION_GROUP_MISSING,
                    assertThrows(CustomException.class, () -> options.resolveSelectedOptions(List.of())).getErrorCode());
        }
    }

    @Nested
    @DisplayName("옵션선택이 있는경우 테스트")
    class ResolveSelectedOptionsTest {

        @Test
        void 필수그룹을_모두_선택하면_선택된_옵션_리스트를_반환한다() {
            // given
            OrderableShopMenuOptions options = createOrderableShopMenuOptions();
            List<Option> selectedOptions = List.of(
                    new Option(1, 1),
                    new Option(2, 3),
                    new Option(2, 4),
                    new Option(3, 6)
            );

            // when
            List<OrderableShopMenuOption> result = options.resolveSelectedOptions(selectedOptions);

            // then
            assertThat(result).hasSize(4);
            assertThat(result).containsExactly(requiredOption1, requiredOption3, requiredOption4, optionalOption1);
        }

        @Test
        void 필수그룹에서_최소선택개수가_부족하면_예외가_발생한다() {
            // given
            OrderableShopMenuOptions options = createOrderableShopMenuOptions();
            List<Option> selectedOptions = List.of(
                    new Option(1, 1),
                    new Option(2, 3), // 필수 그룹2에서는 2개 선택해야하는데 1개만 선택
                    new Option(3, 6)
            );

            // when & then
            assertEquals(ApiResponseCode.MIN_SELECTION_NOT_MET,
                    assertThrows(CustomException.class, () -> options.resolveSelectedOptions(selectedOptions)).getErrorCode());
        }

        @Test
        void 필수그룹이_모두_선택되지_않으면_예외가_발생한다() {
            // given
            OrderableShopMenuOptions options = createOrderableShopMenuOptions();
            List<Option> selectedOptions = List.of(
                    new Option(1, 1),// 필수 그룹2를 선택하지 않음
                    new Option(3, 6)
            );

            // when & then
            assertEquals(ApiResponseCode.REQUIRED_OPTION_GROUP_MISSING,
                    assertThrows(CustomException.class, () -> options.resolveSelectedOptions(selectedOptions)).getErrorCode());
        }

        @Test
        void 옵션그룹에서_최대선택개수를_초과하면_예외가_발생한다() {
            // given
            OrderableShopMenuOptions options = createOrderableShopMenuOptions();
            List<Option> selectedOptions = List.of(
                    new Option(1, 1),
                    new Option(1, 2), // 필수 그룹1 에서는 최대 1개 선택 가능한데 2개 선택
                    new Option(2, 3),
                    new Option(2, 4)
            );

            // when & then
            assertEquals(ApiResponseCode.MAX_SELECTION_EXCEEDED,
                    assertThrows(CustomException.class, () -> options.resolveSelectedOptions(selectedOptions)).getErrorCode());
        }

        @Test
        void 존재하지않는_옵션ID를_선택하면_예외가_발생한다() {
            // given
            OrderableShopMenuOptions options = createOrderableShopMenuOptions();
            List<Option> selectedOptions = List.of(
                    new Option(1, 999),
                    new Option(2, 3),
                    new Option(2, 4)
            );

            // when & then
            assertEquals(ApiResponseCode.NOT_FOUND_ORDERABLE_SHOP_MENU_OPTION,
                    assertThrows(CustomException.class, () -> options.resolveSelectedOptions(selectedOptions)).getErrorCode());
        }

        @Test
        void 잘못된_옵션그룹ID를_선택하면_예외가_발생한다() {
            // given
            OrderableShopMenuOptions options = createOrderableShopMenuOptions();
            List<Option> selectedOptions = List.of(
                    new Option(999, 1), // 잘못된 옵션 그룹 ID
                    new Option(1, 1),
                    new Option(2, 3),
                    new Option(2, 4)
            );

            // when & then
            assertEquals(ApiResponseCode.INVALID_OPTION_IN_GROUP,
                    assertThrows(CustomException.class, () -> options.resolveSelectedOptions(selectedOptions)).getErrorCode());
        }

        @Test
        void 옵션그룹ID와_옵션ID의_조합이_잘못되면_예외가_발생한다() {
            // given
            OrderableShopMenuOptions options = createOrderableShopMenuOptions();
            List<Option> selectedOptions = List.of(
                    new Option(1, 3), // 옵션그룹1에 옵션3은 속하지 않음
                    new Option(2, 3),
                    new Option(2, 4)
            );

            // when & then
            assertEquals(ApiResponseCode.INVALID_OPTION_IN_GROUP,
                    assertThrows(CustomException.class, () -> options.resolveSelectedOptions(selectedOptions)).getErrorCode());
        }
    }


}
