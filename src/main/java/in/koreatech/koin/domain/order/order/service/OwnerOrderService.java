package in.koreatech.koin.domain.order.order.service;

import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_ORDER_STATUS_CHANGE;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_ORDER;
import static in.koreatech.koin.global.code.ApiResponseCode.REQUIRED_ESTIMATED_MINUTES;
import static in.koreatech.koin.global.code.ApiResponseCode.REQUIRED_ORDER_CANCEL_REASON;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import in.koreatech.koin.common.event.OrderNotificationEvent;
import in.koreatech.koin.domain.order.order.dto.request.OwnerOrderStatusChangeRequest;
import in.koreatech.koin.domain.order.order.dto.request.OwnerOrderStatusCriteria;
import in.koreatech.koin.domain.order.order.dto.response.OwnerOrderCountsResponse;
import in.koreatech.koin.domain.order.order.dto.response.OwnerOrderResponse;
import in.koreatech.koin.domain.order.order.dto.response.OwnerOrdersResponse;
import in.koreatech.koin.domain.order.order.model.Order;
import in.koreatech.koin.domain.order.order.repository.OrderRepository;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopRepository;
import in.koreatech.koin.domain.payment.model.entity.Payment;
import in.koreatech.koin.domain.payment.repository.PaymentRepository;
import in.koreatech.koin.domain.payment.service.PaymentCancelService;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerOrderService {

    private final OrderRepository orderRepository;
    private final OrderableShopRepository orderableShopRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentCancelService paymentCancelService;
    private final ApplicationEventPublisher eventPublisher;

    public OwnerOrdersResponse getOrders(
        Integer ownerId,
        Integer orderableShopId,
        OwnerOrderStatusCriteria status
    ) {
        validateShopOwner(ownerId, orderableShopId);
        List<Order> orders = orderRepository.findAllByOrderableShopIdAndStatuses(
            orderableShopId, status.getOrderStatuses());
        return OwnerOrdersResponse.from(orders);
    }

    public OwnerOrderCountsResponse getOrderCounts(Integer ownerId, Integer orderableShopId) {
        validateShopOwner(ownerId, orderableShopId);

        Map<OwnerOrderStatusCriteria, Long> countByCriteria = new EnumMap<>(OwnerOrderStatusCriteria.class);
        for (OwnerOrderStatusCriteria criteria : OwnerOrderStatusCriteria.values()) {
            countByCriteria.put(criteria,
                orderRepository.countByOrderableShopIdAndStatusIn(orderableShopId, criteria.getOrderStatuses()));
        }

        return OwnerOrderCountsResponse.from(countByCriteria);
    }

    public OwnerOrderResponse getOrder(Integer ownerId, Integer orderableShopId, Integer orderId) {
        validateShopOwner(ownerId, orderableShopId);

        Order order = orderRepository.findByIdAndOrderableShopId(orderId, orderableShopId)
            .orElseThrow(() -> CustomException.of(NOT_FOUND_ORDER));

        Payment payment = paymentRepository.getByOrderId(order.getId());
        return OwnerOrderResponse.of(order, payment);
    }

    @Transactional
    public void changeOrderStatus(
        Integer ownerId,
        Integer orderableShopId,
        Integer orderId,
        OwnerOrderStatusChangeRequest request
    ) {
        validateShopOwner(ownerId, orderableShopId);

        Order order = orderRepository.findByIdAndOrderableShopId(orderId, orderableShopId)
            .orElseThrow(() -> CustomException.of(NOT_FOUND_ORDER));
        order.requireStatusChangeableTo(request.status());

        switch (request.status()) {
            case COOKING -> accept(order, request.estimatedMinutes());
            case DELIVERING -> order.startDelivering();
            case DELIVERED -> order.completeDelivery();
            case PACKAGED -> order.completePackaging();
            case PICKED_UP -> order.completePickup();
            case CANCELED -> reject(order, request.canceledReason());
            default -> throw CustomException.of(INVALID_ORDER_STATUS_CHANGE);
        }

        eventPublisher.publishEvent(OrderNotificationEvent.of(
            order.getId(),
            order.getUser().getId(),
            order.getOrderableShopName(),
            order.getStatus().name(),
            order.isDelivery(),
            order.getEstimatedAt()
        ));
    }

    private void accept(Order order, Integer estimatedMinutes) {
        if (estimatedMinutes == null) {
            throw CustomException.of(REQUIRED_ESTIMATED_MINUTES);
        }
        order.startCooking(LocalDateTime.now().plusMinutes(estimatedMinutes));
    }

    private void reject(Order order, String canceledReason) {
        if (!StringUtils.hasText(canceledReason)) {
            throw CustomException.of(REQUIRED_ORDER_CANCEL_REASON);
        }
        paymentCancelService.cancelPaymentByOrderId(order.getId(), canceledReason);
    }

    private void validateShopOwner(Integer ownerId, Integer orderableShopId) {
        OrderableShop orderableShop = orderableShopRepository.getById(orderableShopId);
        orderableShop.requireOwner(ownerId);
    }
}
