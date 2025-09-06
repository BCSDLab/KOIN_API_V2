package in.koreatech.koin.domain.payment.service;

import static in.koreatech.koin.global.code.ApiResponseCode.PAYMENT_CONFIRM_ERROR;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.cart.repository.CartRepository;
import in.koreatech.koin.domain.order.model.Order;
import in.koreatech.koin.domain.order.repository.OrderRepository;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopRepository;
import in.koreatech.koin.domain.payment.dto.response.PaymentConfirmResponse;
import in.koreatech.koin.domain.payment.gateway.pg.PaymentGatewayService;
import in.koreatech.koin.domain.payment.gateway.pg.dto.PaymentGatewayConfirmResponse;
import in.koreatech.koin.domain.payment.mapper.OrderMenuItemsMapper;
import in.koreatech.koin.domain.payment.mapper.PaymentMapper;
import in.koreatech.koin.domain.payment.model.domain.PaymentConfirmInfo;
import in.koreatech.koin.domain.payment.model.entity.Payment;
import in.koreatech.koin.domain.payment.model.entity.PaymentStatus;
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
    private final PaymentRepository paymentRepository;
    private final TemporaryPaymentRedisRepository temporaryPaymentRedisRepository;
    private final CartRepository cartRepository;
    private final PaymentMapper paymentMapper;
    private final OrderMenuItemsMapper orderMenuItemsMapper;

    @Transactional
    public PaymentConfirmResponse confirmPayment(User user, PaymentConfirmInfo paymentConfirmInfo) {
        TemporaryPayment temporaryPayment = temporaryPaymentRedisRepository.getById(paymentConfirmInfo.orderId());
        temporaryPayment.validateMatches(paymentConfirmInfo.orderId(), user.getId(), paymentConfirmInfo.amount());

        PaymentGatewayConfirmResponse pgResponse = paymentGatewayService.confirmPayment(paymentConfirmInfo.paymentKey(),
            paymentConfirmInfo.orderId(), paymentConfirmInfo.amount());
        validatePaymentStatus(pgResponse.status());

        OrderableShop orderableShop = orderableShopRepository.getById(temporaryPayment.getOrderableShopId());
        Order order = temporaryPayment.toOrder(user, orderableShop);
        Cart cart = cartRepository.getCartByUserId(user.getId());
        orderMenuItemsMapper.fromCart(cart).forEach(order::addOrderMenu);
        orderRepository.save(order);

        Payment payment = paymentMapper.toEntity(order, pgResponse);
        paymentRepository.save(payment);

        cleanupAfterPaymentConfirm(paymentConfirmInfo.orderId(), user.getId());

        return PaymentConfirmResponse.of(payment, order);
    }

    private void validatePaymentStatus(String status) {
        PaymentStatus paymentStatus = PaymentStatus.valueOf(status);
        if (!paymentStatus.isDone()) {
            throw CustomException.of(PAYMENT_CONFIRM_ERROR);
        }
    }

    private void cleanupAfterPaymentConfirm(String orderId, Integer userId) {
        temporaryPaymentRedisRepository.deleteById(orderId);
        cartRepository.deleteByUserId(userId);
    }
}
