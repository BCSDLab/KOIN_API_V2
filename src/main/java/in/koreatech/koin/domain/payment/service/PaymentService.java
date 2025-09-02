package in.koreatech.koin.domain.payment.service;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.payment.dto.request.TemporaryDeliveryPaymentSaveRequest;
import in.koreatech.koin.domain.payment.dto.response.TemporaryPaymentResponse;
import in.koreatech.koin.domain.payment.model.domain.DeliveryPaymentInfo;
import in.koreatech.koin.domain.user.model.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserAuthenticationService userAuthenticationService;
    private final TemporaryPaymentService temporaryPaymentService;

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
}
