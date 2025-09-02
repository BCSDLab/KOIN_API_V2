package in.koreatech.koin.domain.payment.service;

import java.util.List;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.cart.repository.CartRepository;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.payment.dto.response.TemporaryPaymentResponse;
import in.koreatech.koin.domain.payment.gateway.pg.PaymentGatewayService;
import in.koreatech.koin.domain.payment.mapper.TemporaryMenuItemsMapper;
import in.koreatech.koin.domain.payment.model.domain.DeliveryPaymentInfo;
import in.koreatech.koin.domain.payment.model.domain.TemporaryMenuItems;
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
    private final TemporaryMenuItemsMapper temporaryMenuItemsMapper;

    @Transactional
    public TemporaryPaymentResponse createDeliveryPayment(User user, DeliveryPaymentInfo deliveryPaymentInfo) {
        Cart cart = cartRepository.getCartByUserId(user.getId());
        OrderableShop orderableShop = cart.getOrderableShop();

        List<TemporaryMenuItems> temporaryMenuItems = temporaryMenuItemsMapper.fromCart(cart);
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
            deliveryPaymentInfo.toOwner(),
            deliveryPaymentInfo.toRider(),
            deliveryPaymentInfo.provideCutlery(),
            totalProductPrice,
            deliveryFee,
            finalAmount,
            temporaryMenuItems
        );

        temporaryPaymentRedisRepository.save(deliveryEntity);
        return TemporaryPaymentResponse.of(pgOrderId);
    }
}
