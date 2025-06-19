package in.koreatech.koin.domain.order.cart.service.implement;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.cart.model.MenuOptions;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOption;
import in.koreatech.koin.domain.order.shop.repository.menu.OrderableShopMenuOptionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OptionGetter {

    private final OrderableShopMenuOptionRepository OrderableShopMenuOptionRepository;

    public MenuOptions getAllByMenuId(Integer menuId) {
        List<OrderableShopMenuOption> allOrderableShopMenuOptionByMenuId = OrderableShopMenuOptionRepository.findAllOrderableShopMenuOptionByMenuId(
            menuId);
        return new MenuOptions(allOrderableShopMenuOptionByMenuId);
    }
}
