package in.koreatech.koin.domain.order.cart.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.cart.dto.CartAddItemCommand;
import in.koreatech.koin.domain.order.order.model.Order;
import in.koreatech.koin.domain.order.order.model.OrderMenu;
import in.koreatech.koin.domain.order.order.model.OrderMenuOption;
import in.koreatech.koin.domain.order.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartReorderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;

    @Transactional
    public void reorderCartItems(Integer orderId, Integer userId) {
        Order order = orderRepository.getByIdAndUserId(orderId, userId);
        List<OrderMenu> orderMenus = order.getOrderMenus();

        for (OrderMenu orderMenu : orderMenus) {
            List<OrderMenuOption> orderMenuOptions = orderMenu.getOrderMenuOptions();
            CartAddItemCommand command = toCommand(userId, order.getOrderableShop().getId(),
                orderMenu.getOrderableShopMenu().getId(), orderMenu.getOrderableShopMenuPrice().getId(),
                orderMenuOptions, orderMenu.getQuantity());
            cartService.addItem(command);
        }
    }

    private CartAddItemCommand toCommand(Integer userId, Integer shopId, Integer shopMenuId, Integer shopMenuPriceId,
        List<OrderMenuOption> orderMenuOptions, Integer quantity) {
        return new CartAddItemCommand(
            userId,
            shopId,
            shopMenuId,
            shopMenuPriceId,
            orderMenuOptions.stream()
                .map(orderMenuOption -> new CartAddItemCommand.Option(
                    orderMenuOption.getOrderableShopMenuOptionGroup().getId(),
                    orderMenuOption.getOrderableShopMenuOption().getId()
                )).toList(),
            quantity
        );
    }
}
