package in.koreatech.koin.domain.order.order.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.cart.repository.CartRepository;
import in.koreatech.koin.domain.order.order.dto.request.TakeoutOrderRequest;
import in.koreatech.koin.domain.order.order.dto.response.OrderCreateResponse;
import in.koreatech.koin.domain.order.order.model.Order;
import in.koreatech.koin.domain.order.order.repository.OrderRepository;
import in.koreatech.koin.domain.order.order.util.OrderPriceValidator;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.payment.mapper.OrderMenuItemsMapper;
import in.koreatech.koin.domain.payment.model.entity.Payment;
import in.koreatech.koin.domain.payment.repository.PaymentRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TakeoutOrderService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderMenuItemsMapper orderMenuItemsMapper;
    private final OrderNumberGenerator orderNumberGenerator;
    private final Clock clock;

    @Transactional
    public OrderCreateResponse createTakeoutOrder(Integer userId, TakeoutOrderRequest request) {
        User user = userRepository.getById(userId);
        Cart cart = cartRepository.getCartByUserId(userId);
        cart.validateTakeoutOrder();
        OrderableShop orderableShop = cart.getOrderableShop();
        orderableShop.getShop().validateBankAccount();

        int totalProductPrice = cart.calculateItemsAmount();
        int totalPrice = totalProductPrice;
        OrderPriceValidator.validatePrice(
            request.totalMenuPrice(),
            request.totalAmount(),
            totalProductPrice,
            totalPrice
        );

        Order order = Order.createTakeout(
            user,
            orderableShop,
            orderNumberGenerator.generate(),
            request.phoneNumber(),
            request.toOwner(),
            request.provideCutlery(),
            totalProductPrice,
            totalPrice
        );
        orderMenuItemsMapper.toOrderMenus(cart, order).forEach(order::addOrderMenu);
        orderRepository.save(order);

        LocalDateTime now = LocalDateTime.now(clock);
        Payment payment = Payment.createBankTransfer(order, now);
        paymentRepository.save(payment);
        cartRepository.delete(cart);

        return OrderCreateResponse.of(order, payment);
    }
}
