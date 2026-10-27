package in.koreatech.koin.domain.order.order.service;

import static in.koreatech.koin.domain.order.delivery.model.AddressType.OFF_CAMPUS;
import static in.koreatech.koin.global.code.ApiResponseCode.ORDER_PRICE_MISMATCH;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.address.service.AddressValidator;
import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.cart.repository.CartRepository;
import in.koreatech.koin.domain.order.order.dto.request.DeliveryOrderRequest;
import in.koreatech.koin.domain.order.order.dto.response.OrderCreateResponse;
import in.koreatech.koin.domain.order.order.model.Order;
import in.koreatech.koin.domain.order.order.repository.OrderRepository;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.payment.mapper.OrderMenuItemsMapper;
import in.koreatech.koin.domain.payment.model.entity.Payment;
import in.koreatech.koin.domain.payment.repository.PaymentRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
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
        cart.validateDeliveryOrder();
        OrderableShop orderableShop = cart.getOrderableShop();
        orderableShop.getShop().validateBankAccount();

        int totalProductPrice = cart.calculateItemsAmount();
        int deliveryFee = orderableShop.calculateDeliveryFee(totalProductPrice);
        int totalPrice = totalProductPrice + deliveryFee;
        validatePrice(
            request.totalMenuPrice(),
            request.deliveryTip(),
            request.totalAmount(),
            totalProductPrice,
            deliveryFee,
            totalPrice
        );

        Order order = Order.createDelivery(
            user,
            orderableShop,
            orderNumberGenerator.generate(),
            request.phoneNumber(),
            request.address(),
            request.addressDetail(),
            request.longitude(),
            request.latitude(),
            request.toOwner(),
            request.toRider(),
            request.provideCutlery(),
            totalProductPrice,
            deliveryFee,
            totalPrice
        );
        orderMenuItemsMapper.toOrderMenus(cart, order).forEach(order::addOrderMenu);
        orderRepository.save(order);

        Payment payment = Payment.createBankTransfer(order);
        paymentRepository.save(payment);
        cartRepository.delete(cart);

        return OrderCreateResponse.of(order, payment);
    }

    private void validatePrice(
        int requestedMenuPrice,
        int requestedDeliveryTip,
        int requestedTotalAmount,
        int totalProductPrice,
        int deliveryFee,
        int totalPrice
    ) {
        if (requestedMenuPrice != totalProductPrice || requestedDeliveryTip != deliveryFee || requestedTotalAmount != totalPrice) {
            throw CustomException.of(ORDER_PRICE_MISMATCH);
        }
    }
}
