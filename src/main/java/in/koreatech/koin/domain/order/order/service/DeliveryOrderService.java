package in.koreatech.koin.domain.order.order.service;

import static in.koreatech.koin.domain.order.delivery.model.AddressType.OFF_CAMPUS;
import static in.koreatech.koin.domain.order.order.model.OrderStatus.CONFIRMING;
import static in.koreatech.koin.domain.order.order.model.OrderType.DELIVERY;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import in.koreatech.koin.domain.address.service.AddressValidator;
import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.cart.repository.CartRepository;
import in.koreatech.koin.domain.order.order.dto.request.DeliveryOrderRequest;
import in.koreatech.koin.domain.order.order.dto.response.OrderCreateResponse;
import in.koreatech.koin.domain.order.order.model.Order;
import in.koreatech.koin.domain.order.order.model.OrderDelivery;
import in.koreatech.koin.domain.order.order.repository.OrderRepository;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.payment.mapper.OrderMenuItemsMapper;
import in.koreatech.koin.domain.payment.model.domain.DeliveryPaymentInfo;
import in.koreatech.koin.domain.payment.model.entity.Payment;
import in.koreatech.koin.domain.payment.repository.PaymentRepository;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryOrderService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderMenuItemsMapper orderMenuItemsMapper;
    private final OrderNumberGenerator orderNumberGenerator;
    private final AddressValidator addressValidator;

    @Transactional
    public OrderCreateResponse createDeliveryOrder(Integer userId, DeliveryOrderRequest request) {
        User user = userRepository.getById(userId);
        if (request.deliveryType() == OFF_CAMPUS) {
            addressValidator.validateAddress(request.address());
        }

        Cart cart = cartRepository.getCartByUserId(userId);
        validateCart(cart);
        OrderableShop orderableShop = cart.getOrderableShop();
        validateBankAccount(orderableShop.getShop());

        int totalProductPrice = cart.calculateItemsAmount();
        int deliveryFee = orderableShop.calculateDeliveryFee(totalProductPrice);
        int totalPrice = totalProductPrice + deliveryFee;
        DeliveryPaymentInfo.of(
            request.phoneNumber(), request.address(), request.addressDetail(), request.longitude(), request.latitude(),
            request.toOwner(), request.toRider(), request.provideCutlery(), request.totalMenuPrice(),
            request.deliveryTip(), request.totalAmount()
        ).validatePrice(totalProductPrice, deliveryFee, totalPrice);

        Order order = createOrder(user, orderableShop, request, totalProductPrice, deliveryFee, totalPrice);
        orderMenuItemsMapper.toOrderMenus(cart, order).forEach(order::addOrderMenu);
        orderRepository.save(order);

        Payment payment = Payment.createBankTransfer(order);
        paymentRepository.save(payment);
        cartRepository.delete(cart);

        return OrderCreateResponse.of(order, payment);
    }

    private void validateCart(Cart cart) {
        if (cart.getCartMenuItems().isEmpty()) {
            throw CustomException.of(ApiResponseCode.NOT_FOUND_CART_ITEM);
        }
        cart.validateOrderType(DELIVERY);
        cart.getOrderableShop().requireShopOpen();
        cart.getOrderableShop().requireMinimumOrderAmount(cart.calculateItemsAmount());
        cart.getCartMenuItems().forEach(item -> item.getOrderableShopMenu().validateSoldOut());
    }

    private void validateBankAccount(Shop shop) {
        if (!shop.isPayBank() || !StringUtils.hasText(shop.getBank())
            || !StringUtils.hasText(shop.getAccountNumber()) || !StringUtils.hasText(shop.getAccountHolder())) {
            throw CustomException.of(ApiResponseCode.SHOP_BANK_ACCOUNT_UNAVAILABLE);
        }
    }

    private Order createOrder(User user, OrderableShop orderableShop, DeliveryOrderRequest request,
        int totalProductPrice, int deliveryFee, int totalPrice) {
        Shop shop = orderableShop.getShop();
        Order order = Order.builder()
            .orderNumber(orderNumberGenerator.generate())
            .orderType(DELIVERY)
            .status(CONFIRMING)
            .orderableShopAddress(shop.getAddress())
            .orderableShopAddressDetail(shop.getAddressDetail())
            .orderableShopName(shop.getName())
            .phoneNumber(request.phoneNumber())
            .totalProductPrice(totalProductPrice)
            .discountAmount(0)
            .totalPrice(totalPrice)
            .orderableShop(orderableShop)
            .user(user)
            .isDeleted(false)
            .build();
        order.setOrderDelivery(OrderDelivery.builder()
            .order(order)
            .address(request.address())
            .addressDetail(request.addressDetail())
            .longitude(request.longitude())
            .latitude(request.latitude())
            .toOwner(request.toOwner())
            .toRider(request.toRider())
            .provideCutlery(request.provideCutlery())
            .deliveryTip(deliveryFee)
            .build());
        return order;
    }
}
