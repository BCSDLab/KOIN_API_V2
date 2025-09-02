package in.koreatech.koin.domain.payment.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.domain.payment.dto.request.TemporaryDeliveryPaymentSaveRequest;
import in.koreatech.koin.domain.payment.dto.response.TemporaryPaymentResponse;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RequestMapping("/payments")
@Tag(name = "(Normal) Payments: 결제", description = "결제 API를 관리한다.")
public interface PaymentApi {

    @Operation(
        summary = "임시 배달 결제 정보를 저장한다",
        description = """
            ## 임시 배달 결제 정보 저장
            토큰을 통해 장바구니를 불러옵니다.
            요청 본문과 장바구니 데이터를 함께 임시 저장합니다. 저장 기간은 10분입니다.
            
            반환 값인 주문번호를 사용하여 로직을 수행해주세요.
            
            ## 요청 Body 필드 설명
            - `address`: 배달 받을 주소 (필수)
            - `phone_number`: 수신자 연락처 (필수)
            - `to_owner`: 사장님께 전달할 메시지 (선택)
            - `to_rider`: 라이더에게 전달할 메시지 (선택)
            - `total_menu_price`: 메뉴 총 금액 (필수)
            - `delivery_tip`: 배달 팁 (필수)
            - `total_amount`: 총 결제 금액 (필수)
            """
    )
    @PostMapping("/delivery/temporary")
    ResponseEntity<TemporaryPaymentResponse> createTemporaryDeliveryPayment(
        @RequestBody @Valid TemporaryDeliveryPaymentSaveRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    );
}
