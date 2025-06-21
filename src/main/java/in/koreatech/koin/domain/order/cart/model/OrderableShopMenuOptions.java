package in.koreatech.koin.domain.order.cart.model;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;

import in.koreatech.koin.domain.order.cart.dto.CartAddItemCommand.Option;
import in.koreatech.koin.domain.order.cart.exception.CartErrorCode;
import in.koreatech.koin.domain.order.cart.exception.CartException;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOptionGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderableShopMenuOptions {

    private List<OrderableShopMenuOption> options;

    public List<OrderableShopMenuOption> resolveSelectedOptions(List<Option> selectedOptions) {
        if (selectedOptions == null || selectedOptions.isEmpty()) {
            // 필수 옵션 그룹 선택 검증
            validateRequiredGroupAreNotSelected();
            return List.of();
        }

        Map<Integer, OrderableShopMenuOption> optionsMap = options.stream()
            .collect(toMap(OrderableShopMenuOption::getId, option -> option));

        // 요청한 옵션 그룹과 옵션의 유효성 검증
        validateOptionIntegrity(selectedOptions, optionsMap);

        Map<Integer, List<Option>> selectedOptionsByGroupId  = selectedOptions.stream()
            .collect(groupingBy(Option::optionGroupId));

        // 옵션 그룹별 최소/최대 선택, 필수 선택 규칙 검증
        validateOptionGroupRules(selectedOptionsByGroupId);

        return selectedOptions.stream()
            .map(selectedOption -> optionsMap.get(selectedOption.optionId()))
            .toList();
    }

    private void validateOptionIntegrity(List<Option> selectedOptions, Map<Integer, OrderableShopMenuOption> optionsMap) {
        for(Option selectedOption: selectedOptions) {
            OrderableShopMenuOption optionInDB = optionsMap.get(selectedOption.optionId());

            // 메뉴에 존재 하지 않는 옵션 ID인 경우
            if (optionInDB == null) {
                throw new CartException(CartErrorCode.MENU_OPTION_NOT_FOUND);
            }

            // 요청한 옵션 그룹 ID가 실제 해당 옵션의 옵션 그룹 ID와 일치 하지 않는 경우
            if (!optionInDB.getOptionGroup().getId().equals(selectedOption.optionGroupId())) {
                throw new CartException(CartErrorCode.INVALID_OPTION_IN_GROUP);
            }
        }
    }

    private void validateRequiredGroupAreNotSelected() {
        options.stream()
            .map(OrderableShopMenuOption::getOptionGroup)
            .distinct()
            .filter(OrderableShopMenuOptionGroup::getIsRequired)
            .findFirst()
            .ifPresent(group -> {
                throw new CartException(CartErrorCode.REQUIRED_OPTION_GROUP_MISSING);
            });
    }

    private void validateOptionGroupRules(Map<Integer, List<Option>> selectedOptionsByGroupId) {
        Map<OrderableShopMenuOptionGroup, List<OrderableShopMenuOption>> availableOptionsByGroup  = options.stream()
            .collect(groupingBy(OrderableShopMenuOption::getOptionGroup));

        for(OrderableShopMenuOptionGroup optionGroup: availableOptionsByGroup.keySet()) {
            List<Option> userSelectionsForGroup = selectedOptionsByGroupId.getOrDefault(optionGroup.getId(), List.of());
            optionGroup.validateSelectionCount(userSelectionsForGroup.size());
        }
    }
}
