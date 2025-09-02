package in.koreatech.koin.domain.payment.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.payment.dto.request.PaymentCancelRequest;
import in.koreatech.koin.domain.payment.dto.request.PaymentConfirmRequest;
import in.koreatech.koin.domain.payment.dto.request.TemporaryDeliveryPaymentSaveRequest;
import in.koreatech.koin.domain.payment.dto.request.TemporaryTakeoutPaymentSaveRequest;
import in.koreatech.koin.domain.payment.dto.response.PaymentCancelResponse;
import in.koreatech.koin.domain.payment.dto.response.PaymentConfirmResponse;
import in.koreatech.koin.domain.payment.dto.response.PaymentResponse;
import in.koreatech.koin.domain.payment.dto.response.TemporaryPaymentResponse;
import in.koreatech.koin.domain.payment.service.PaymentService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController implements PaymentApi {

    private final PaymentService paymentService;

    @PostMapping("/delivery/temporary")
    public ResponseEntity<TemporaryPaymentResponse> createTemporaryDeliveryPayment(
        @RequestBody @Valid final TemporaryDeliveryPaymentSaveRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TemporaryPaymentResponse response = paymentService.createTemporaryDeliveryPayment(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/takeout/temporary")
    public ResponseEntity<TemporaryPaymentResponse> createTemporaryTakeoutPayment(
        @RequestBody @Valid final TemporaryTakeoutPaymentSaveRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TemporaryPaymentResponse response = paymentService.createTemporaryTakeoutPayment(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResponse> confirmPayment(
        @RequestBody @Valid final PaymentConfirmRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        PaymentConfirmResponse response = paymentService.confirmPayment(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentCancelResponse> cancelPayment(
        @PathVariable(value = "paymentId") final Integer paymentId,
        @RequestBody @Valid final PaymentCancelRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        PaymentCancelResponse response = paymentService.cancelPayment(userId, paymentId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
        @PathVariable(value = "paymentId") final Integer paymentId,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        PaymentResponse response = paymentService.getPayment(userId, paymentId);
        return ResponseEntity.ok(response);
    }
}
