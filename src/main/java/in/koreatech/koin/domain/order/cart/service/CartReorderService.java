package in.koreatech.koin.domain.order.cart.service;

import static in.koreatech.koin.domain.order.cart.dto.CartAddItemCommand.Option;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.cart.model.OrderableShopMenuOptions;
import in.koreatech.koin.domain.order.cart.model.OrderableShopMenus;
import in.koreatech.koin.domain.order.cart.repository.CartRepository;
import in.koreatech.koin.domain.order.order.model.Order;
import in.koreatech.koin.domain.order.order.model.OrderMenu;
import in.koreatech.koin.domain.order.order.model.OrderMenuOption;
import in.koreatech.koin.domain.order.order.repository.OrderRepository;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOption;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.order.shop.repository.menu.OrderableShopMenuOptionRepository;
import in.koreatech.koin.domain.order.shop.repository.menu.OrderableShopMenuRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartReorderService {

    private final OrderableShopMenuOptionRepository orderableShopMenuOptionRepository;
    private final OrderableShopMenuRepository orderableShopMenuRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    @Transactional
    public void reorderCartItems(Integer orderId, Integer userId) {
        Order order = orderRepository.getByIdAndUserId(orderId, userId);
        OrderableShop orderableShop = validateShopAndGetShop(order);
        Cart cart = getOrCreateCart(userId, orderableShop);

        List<OrderMenu> orderMenus = order.getOrderMenus();

        for (OrderMenu orderMenu : orderMenus) {
            List<OrderMenuOption> orderMenuOptions = orderMenu.getOrderMenuOptions();
            OrderableShopMenu orderableShopMenu = validateAndGetMenu(orderableShop, orderMenu);
            List<OrderableShopMenuOption> orderableShopMenuOptions = validateAndGetOptions(orderMenu, orderMenuOptions);
            cart.addItem(orderableShopMenu,
                orderableShopMenu.getMenuPriceById(orderMenu.getOrderableShopMenuPrice().getId()),
                orderableShopMenuOptions, orderMenu.getQuantity());
        }
    }

    private OrderableShop validateShopAndGetShop(Order order) {
        OrderableShop orderableShop = order.getOrderableShop();
        orderableShop.requireShopOpen();
        return orderableShop;
    }

    private Cart getOrCreateCart(Integer userId, OrderableShop orderableShop) {
        Cart cart = cartRepository.findCartByUserId(userId).orElse(
            Cart.from(userRepository.getById(userId), orderableShop)
        );
        cart.validateSameShop(orderableShop.getId());
        return cart;
    }

    private OrderableShopMenu validateAndGetMenu(OrderableShop orderableShop, OrderMenu orderMenu) {
        OrderableShopMenus shopOrderableShopMenus = orderableShopMenuRepository.getAllByOrderableShopId(orderableShop.getId());
        OrderableShopMenu menu = shopOrderableShopMenus.resolveSelectedMenu(orderMenu.getOrderableShopMenu().getId());

        menu.validateSoldOut();
        menu.requiredMenuPriceById(orderMenu.getOrderableShopMenuPrice().getId());

        return menu;
    }

    private List<OrderableShopMenuOption> validateAndGetOptions(
        OrderMenu orderMenu, List<OrderMenuOption> orderMenuOptions
    ) {
        OrderableShopMenuOptions shopMenuOptions = orderableShopMenuOptionRepository.getAllByMenuId(
            orderMenu.getOrderableShopMenu().getId());
        return shopMenuOptions.resolveSelectedOptions(orderMenuOptions.stream()
            .map(orderMenuOption -> new Option(
                orderMenuOption.getOrderableShopMenuOptionGroup().getId(),
                orderMenuOption.getOrderableShopMenuOption().getId()
            )).toList()
        );
    }
}
