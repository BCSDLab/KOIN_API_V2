package in.koreatech.koin.domain.payment.service;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.cart.repository.CartRepository;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.payment.dto.response.TemporaryPaymentResponse;
import in.koreatech.koin.domain.payment.gateway.pg.PaymentGatewayService;
import in.koreatech.koin.domain.payment.model.domain.DeliveryPaymentInfo;
import in.koreatech.koin.domain.payment.model.domain.TakeoutPaymentInfo;
import in.koreatech.koin.domain.payment.model.redis.TemporaryPayment;
import in.koreatech.koin.domain.payment.repository.TemporaryPaymentRedisRepository;
import in.koreatech.koin.domain.user.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TemporaryPaymentService {

    private final CartRepository cartRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final TemporaryPaymentRedisRepository temporaryPaymentRedisRepository;

    @Transactional
    public TemporaryPaymentResponse createDeliveryPayment(User user, DeliveryPaymentInfo deliveryPaymentInfo) {
        Cart cart = cartRepository.getCartByUserId(user.getId());
        OrderableShop orderableShop = cart.getOrderableShop();

        int totalProductPrice = cart.calculateItemsAmount();
        int deliveryFee = orderableShop.calculateDeliveryFee(totalProductPrice);
        int finalAmount = totalProductPrice + deliveryFee;

        deliveryPaymentInfo.validatePrice(totalProductPrice, deliveryFee, finalAmount);

        String pgOrderId = paymentGatewayService.generatePgOrderId();
        TemporaryPayment deliveryEntity = TemporaryPayment.toDeliveryEntity(
            pgOrderId,
            user.getId(),
            orderableShop.getId(),
            deliveryPaymentInfo.phoneNumber(),
            deliveryPaymentInfo.address(),
            deliveryPaymentInfo.addressDetail(),
            deliveryPaymentInfo.longitude(),
            deliveryPaymentInfo.latitude(),
            deliveryPaymentInfo.toOwner(),
            deliveryPaymentInfo.toRider(),
            deliveryPaymentInfo.provideCutlery(),
            totalProductPrice,
            deliveryFee,
            finalAmount
        );

        temporaryPaymentRedisRepository.save(deliveryEntity);
        return TemporaryPaymentResponse.of(pgOrderId);
    }

    @Transactional
    public TemporaryPaymentResponse createTakeoutPayment(User user, TakeoutPaymentInfo takeoutPaymentInfo) {
        Cart cart = cartRepository.getCartByUserId(user.getId());
        OrderableShop orderableShop = cart.getOrderableShop();

        int totalProductPrice = cart.calculateItemsAmount();
        int finalAmount = totalProductPrice;

        takeoutPaymentInfo.validatePrice(totalProductPrice, finalAmount);

        String pgOrderId = paymentGatewayService.generatePgOrderId();
        TemporaryPayment takeoutEntity = TemporaryPayment.toTakeOutEntity(
            pgOrderId,
            user.getId(),
            orderableShop.getId(),
            takeoutPaymentInfo.phoneNumber(),
            takeoutPaymentInfo.toOwner(),
            takeoutPaymentInfo.provideCutlery(),
            totalProductPrice,
            finalAmount
        );

        temporaryPaymentRedisRepository.save(takeoutEntity);
        return TemporaryPaymentResponse.of(pgOrderId);
    }
}
