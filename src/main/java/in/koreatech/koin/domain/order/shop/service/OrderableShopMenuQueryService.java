package in.koreatech.koin.domain.order.shop.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.shop.dto.menu.OrderableShopMenuResponse;
import in.koreatech.koin.domain.order.shop.dto.menu.OrderableShopMenusResponse;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopRepository;
import in.koreatech.koin.domain.order.shop.repository.menu.OrderableShopMenuRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderableShopMenuQueryService {

    private final OrderableShopRepository orderableShopRepository;
    private final OrderableShopMenuRepository orderableShopMenuRepository;

    public List<OrderableShopMenusResponse> getOrderableShopMenus(Integer orderableShopId) {
        OrderableShop orderableShop = orderableShopRepository.getByIdWithMenus(orderableShopId);

        return orderableShop.getMenuGroups().stream()
            .map(OrderableShopMenusResponse::from)
            .toList();
    }

    public OrderableShopMenuResponse getOrderableShopMenu(Integer orderableShopMenuId) {
        OrderableShopMenu orderableShopMenu = orderableShopMenuRepository.getByIdWithMenuOptionGroups(
            orderableShopMenuId);
        return OrderableShopMenuResponse.from(orderableShopMenu);
    }
}
