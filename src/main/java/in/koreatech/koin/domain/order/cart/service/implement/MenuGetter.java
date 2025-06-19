package in.koreatech.koin.domain.order.cart.service.implement;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.cart.model.Menus;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import in.koreatech.koin.domain.order.shop.repository.menu.OrderableShopMenuRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuGetter {

    private final OrderableShopMenuRepository orderableShopMenuRepository;

    public OrderableShopMenu get(Integer menuId) {
        return orderableShopMenuRepository.getByIdWithMenuOptionGroups(menuId);
    }

    public Menus getAllByOrderableShopId(Integer orderableShopId) {
        List<OrderableShopMenu> menus = orderableShopMenuRepository.getAllByOrderableShop(orderableShopId);
        return new Menus(orderableShopId, menus);
    }
}
