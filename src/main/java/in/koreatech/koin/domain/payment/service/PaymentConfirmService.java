package in.koreatech.koin.domain.payment.service;

import static in.koreatech.koin.global.code.ApiResponseCode.PAYMENT_CONFIRM_ERROR;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.cart.repository.CartRepository;
import in.koreatech.koin.domain.order.model.Order;
import in.koreatech.koin.domain.order.model.OrderMenu;
import in.koreatech.koin.domain.order.model.Payment;
import in.koreatech.koin.domain.order.model.PaymentStatus;
import in.koreatech.koin.domain.order.repository.OrderMenuRepository;
import in.koreatech.koin.domain.order.repository.OrderRepository;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopRepository;
import in.koreatech.koin.domain.payment.dto.response.PaymentConfirmResponse;
import in.koreatech.koin.domain.payment.gateway.pg.PaymentGatewayService;
import in.koreatech.koin.domain.payment.gateway.pg.dto.PaymentGatewayConfirmResponse;
import in.koreatech.koin.domain.payment.mapper.PaymentMapper;
import in.koreatech.koin.domain.payment.model.domain.PaymentConfirmInfo;
import in.koreatech.koin.domain.payment.model.redis.TemporaryPayment;
import in.koreatech.koin.domain.payment.repository.PaymentRepository;
import in.koreatech.koin.domain.payment.repository.TemporaryPaymentRedisRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentConfirmService {

    private final PaymentGatewayService paymentGatewayService;
    private final OrderableShopRepository orderableShopRepository;
    private final OrderRepository orderRepository;
    private final OrderMenuRepository orderMenuRepository;
    private final PaymentRepository paymentRepository;
    private final TemporaryPaymentRedisRepository temporaryPaymentRedisRepository;
    private final CartRepository cartRepository;
    private final PaymentMapper paymentMapper;

    @Transactional
    public PaymentConfirmResponse confirmPayment(User user, PaymentConfirmInfo paymentConfirmInfo) {
        TemporaryPayment temporaryPayment = temporaryPaymentRedisRepository.getById(paymentConfirmInfo.orderId());
        temporaryPayment.validateMatches(paymentConfirmInfo.orderId(), user.getId(), paymentConfirmInfo.amount());

        PaymentGatewayConfirmResponse pgResponse = paymentGatewayService.confirmPayment(paymentConfirmInfo.paymentKey(),
            paymentConfirmInfo.orderId(), paymentConfirmInfo.amount());
        validatePaymentStatus(pgResponse.status());

        OrderableShop orderableShop = orderableShopRepository.getById(temporaryPayment.getOrderableShopId());
        Order order = temporaryPayment.toOrder(user, orderableShop);
        orderRepository.save(order);
        List<OrderMenu> orderMenus = createOrderMenus(temporaryPayment, order);
        orderMenuRepository.saveAll(orderMenus);

        Payment payment = paymentMapper.toEntity(order, pgResponse);
        paymentRepository.save(payment);

        cleanupAfterPaymentConfirm(paymentConfirmInfo.orderId(), user.getId());

        return PaymentConfirmResponse.of(payment, order, orderMenus);
    }

    private void validatePaymentStatus(String status) {
        PaymentStatus paymentStatus = PaymentStatus.valueOf(status);
        if (!paymentStatus.isDone()) {
            throw CustomException.of(PAYMENT_CONFIRM_ERROR);
        }
    }

    private List<OrderMenu> createOrderMenus(TemporaryPayment temporaryPayment, Order order) {
        return temporaryPayment.getTemporaryMenuItems().stream()
            .map(temporaryMenuItems -> temporaryMenuItems.toOrderMenu(order))
            .toList();
    }

    private void cleanupAfterPaymentConfirm(String orderId, Integer userId) {
        temporaryPaymentRedisRepository.deleteById(orderId);
        cartRepository.deleteByUserId(userId);
    }
}
