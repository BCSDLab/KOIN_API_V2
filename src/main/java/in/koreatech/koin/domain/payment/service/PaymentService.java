package in.koreatech.koin.domain.payment.service;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.payment.dto.request.PaymentCancelRequest;
import in.koreatech.koin.domain.payment.dto.request.PaymentConfirmRequest;
import in.koreatech.koin.domain.payment.dto.request.TemporaryDeliveryPaymentSaveRequest;
import in.koreatech.koin.domain.payment.dto.request.TemporaryTakeoutPaymentSaveRequest;
import in.koreatech.koin.domain.payment.dto.response.PaymentCancelResponse;
import in.koreatech.koin.domain.payment.dto.response.PaymentConfirmResponse;
import in.koreatech.koin.domain.payment.dto.response.TemporaryPaymentResponse;
import in.koreatech.koin.domain.payment.model.domain.DeliveryPaymentInfo;
import in.koreatech.koin.domain.payment.model.domain.PaymentCancelInfo;
import in.koreatech.koin.domain.payment.model.domain.PaymentConfirmInfo;
import in.koreatech.koin.domain.payment.model.domain.TakeoutPaymentInfo;
import in.koreatech.koin.domain.user.model.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserAuthenticationService userAuthenticationService;
    private final TemporaryPaymentService temporaryPaymentService;
    private final PaymentConfirmService paymentConfirmService;
    private final PaymentCancelService paymentCancelService;

    public TemporaryPaymentResponse createTemporaryDeliveryPayment(
        Integer userId, TemporaryDeliveryPaymentSaveRequest request
    ) {
        User user = userAuthenticationService.authenticateUser(userId);
        DeliveryPaymentInfo deliveryPaymentInfo = DeliveryPaymentInfo.of(
            request.phoneNumber(),
            request.address(),
            request.toOwner(),
            request.toRider(),
            request.provideCutlery(),
            request.totalMenuPrice(),
            request.deliveryTip(),
            request.totalAmount()
        );
        return temporaryPaymentService.createDeliveryPayment(user, deliveryPaymentInfo);
    }

    public TemporaryPaymentResponse createTemporaryTakeoutPayment(
        Integer userId, TemporaryTakeoutPaymentSaveRequest request
    ) {
        User user = userAuthenticationService.authenticateUser(userId);
        TakeoutPaymentInfo takeoutPaymentInfo = TakeoutPaymentInfo.of(
            request.phoneNumber(),
            request.toOwner(),
            request.provideCutlery(),
            request.totalMenuPrice(),
            request.totalAmount()
        );
        return temporaryPaymentService.createTakeoutPayment(user, takeoutPaymentInfo);
    }

    public PaymentConfirmResponse confirmPayment(Integer userId, PaymentConfirmRequest request) {
        User user = userAuthenticationService.authenticateUser(userId);
        PaymentConfirmInfo paymentConfirmInfo = PaymentConfirmInfo.of(
            request.paymentKey(),
            request.orderId(),
            request.amount()
        );
        return paymentConfirmService.confirmPayment(user, paymentConfirmInfo);
    }

    public PaymentCancelResponse cancelPayment(Integer userId, Integer paymentId, PaymentCancelRequest request) {
        User user = userAuthenticationService.authenticateUser(userId);
        PaymentCancelInfo paymentCancelInfo = PaymentCancelInfo.of(request.cancelReason());
        return paymentCancelService.cancelPayment(user, paymentId, paymentCancelInfo);
    }
}
