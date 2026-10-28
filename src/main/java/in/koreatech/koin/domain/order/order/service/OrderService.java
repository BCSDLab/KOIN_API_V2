package in.koreatech.koin.domain.order.order.service;

import static in.koreatech.koin.domain.order.delivery.model.AddressType.OFF_CAMPUS;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.address.service.AddressValidator;
import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.cart.repository.CartRepository;
import in.koreatech.koin.domain.order.order.dto.request.DeliveryOrderRequest;
import in.koreatech.koin.domain.order.order.dto.request.OrderSearchCondition;
import in.koreatech.koin.domain.order.order.dto.request.TakeoutOrderRequest;
import in.koreatech.koin.domain.order.order.dto.response.InprogressOrderResponse;
import in.koreatech.koin.domain.order.order.dto.response.OrderCreateResponse;
import in.koreatech.koin.domain.order.order.dto.response.OrdersResponse;
import in.koreatech.koin.domain.order.order.model.Order;
import in.koreatech.koin.domain.order.order.model.OrderInfo;
import in.koreatech.koin.domain.order.order.model.OrderSearchCriteria;
import in.koreatech.koin.domain.order.order.model.OrderStatus;
import in.koreatech.koin.domain.order.order.repository.OrderRepository;
import in.koreatech.koin.domain.order.order.repository.OrderSearchQueryRepository;
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
@Transactional(readOnly = true)
public class OrderService {

    private final OrderSearchQueryRepository orderSearchQueryRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final OrderMenuItemsMapper orderMenuItemsMapper;
    private final OrderNumberGenerator orderNumberGenerator;
    private final AddressValidator addressValidator;
    private final Clock clock;

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
        OrderPriceValidator.validatePrice(
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
        return saveOrder(order, cart);
    }

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
        return saveOrder(order, cart);
    }

    public OrdersResponse getOrders(Integer userId, OrderSearchCondition orderSearchCondition) {
        OrderSearchCriteria orderSearchCriteria = OrderSearchCriteria.from(orderSearchCondition);
        Page<OrderInfo> orderPage = orderSearchQueryRepository.findOrdersByCondition(userId, orderSearchCriteria);
        return OrdersResponse.of(orderPage);
    }

    public List<InprogressOrderResponse> getInprogressOrders(Integer userId) {
        User user = userRepository.getById(userId);
        List<Order> orders = orderRepository.findAllByUserIdAndStatuses(
            user.getId(), OrderStatus.inProgressStatuses());

        return orders.stream()
            .map(order -> {
                Payment payment = paymentRepository.getByOrderId(order.getId());
                return InprogressOrderResponse.from(order, payment);
            })
            .toList();
    }

    private OrderCreateResponse saveOrder(Order order, Cart cart) {
        orderMenuItemsMapper.toOrderMenus(cart, order).forEach(order::addOrderMenu);
        orderRepository.save(order);

        LocalDateTime now = LocalDateTime.now(clock);
        Payment payment = Payment.createBankTransfer(order, now);
        paymentRepository.save(payment);
        cartRepository.delete(cart);

        return OrderCreateResponse.of(order, payment);
    }
}
