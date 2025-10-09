package in.koreatech.koin.domain.payment.mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.order.order.model.Order;
import in.koreatech.koin.domain.payment.model.entity.Payment;
import in.koreatech.koin.domain.payment.model.entity.PaymentMethod;
import in.koreatech.koin.domain.payment.model.entity.PaymentStatus;
import in.koreatech.koin.domain.payment.gateway.pg.dto.PaymentGatewayConfirmResponse;

@Component
public class PaymentMapper {

    public Payment toEntity(Order order, PaymentGatewayConfirmResponse response) {
        OffsetDateTime requestedOffsetDateTime = OffsetDateTime.parse(response.requestedAt());
        OffsetDateTime approvedOffsetDateTime = OffsetDateTime.parse(response.approvedAt());

        LocalDateTime requested = requestedOffsetDateTime.toLocalDateTime();
        LocalDateTime approved = approvedOffsetDateTime.toLocalDateTime();

        return Payment.builder()
            .paymentKey(response.paymentKey())
            .amount(response.totalAmount())
            .paymentStatus(PaymentStatus.valueOf(response.status()))
            .paymentMethod(PaymentMethod.from(response.method()))
            .description(response.orderName())
            .easyPayCompany(response.provider())
            .requestedAt(requested)
            .approvedAt(approved)
            .receipt(response.receipt())
            .isDeleted(false)
            .order(order)
            .build();
    }
}
