package in.koreatech.koin.domain.order.order.service;

import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_ORDER;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerOrderService {

    private final OrderRepository orderRepository;
    private final OrderableShopRepository orderableShopRepository;
    private final PaymentRepository paymentRepository;

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

        Order order = orderRepository.findDetailById(orderId)
            .orElseThrow(() -> CustomException.of(NOT_FOUND_ORDER));
        validateOrderBelongsToShop(order, orderableShopId);

        Payment payment = paymentRepository.getByOrderId(order.getId());
        return OwnerOrderResponse.of(order, payment);
    }

    private void validateShopOwner(Integer ownerId, Integer orderableShopId) {
        OrderableShop orderableShop = orderableShopRepository.getById(orderableShopId);
        orderableShop.requireOwner(ownerId);
    }

    private void validateOrderBelongsToShop(Order order, Integer orderableShopId) {
        if (!order.isOrderedAt(orderableShopId)) {
            throw CustomException.of(NOT_FOUND_ORDER);
        }
    }
}
