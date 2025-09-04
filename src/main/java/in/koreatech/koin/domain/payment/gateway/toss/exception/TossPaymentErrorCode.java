package in.koreatech.koin.domain.payment.gateway.toss.exception;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum TossPaymentErrorCode {

    // 정의되지 않은 에러 코드인 경우 해당 에러코드를 내린다.
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러가 발생했습니다. 관리자에게 문의해주세요."),

    // 결제 승인 API 에러 코드
    // 400
    ALREADY_PROCESSED_PAYMENT(HttpStatus.BAD_REQUEST, "이미 처리된 결제입니다."),
    PROVIDER_ERROR(HttpStatus.BAD_REQUEST, "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    EXCEED_MAX_CARD_INSTALLMENT_PLAN(HttpStatus.BAD_REQUEST, "설정 가능한 최대 할부 개월 수를 초과했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    NOT_ALLOWED_POINT_USE(HttpStatus.BAD_REQUEST, "포인트 사용이 불가한 카드로 카드 포인트 결제에 실패했습니다."),
    INVALID_API_KEY(HttpStatus.BAD_REQUEST, "결제 서버에 오류가 발생했습니다."),
    INVALID_REJECT_CARD(HttpStatus.BAD_REQUEST, "카드 사용이 거절되었습니다. 카드사 문의가 필요합니다."),
    BELOW_MINIMUM_AMOUNT(HttpStatus.BAD_REQUEST, "결제금액이 최소 허용 금액보다 적습니다."),
    INVALID_CARD_EXPIRATION(HttpStatus.BAD_REQUEST, "카드 유효기간을 다시 확인해주세요."),
    INVALID_STOPPED_CARD(HttpStatus.BAD_REQUEST, "정지된 카드입니다."),
    EXCEED_MAX_DAILY_PAYMENT_COUNT(HttpStatus.BAD_REQUEST, "하루 결제 가능 횟수를 초과했습니다."),
    NOT_SUPPORTED_INSTALLMENT_PLAN_CARD_OR_MERCHANT(HttpStatus.BAD_REQUEST, "할부가 지원되지 않는 카드 또는 가맹점입니다."),
    INVALID_CARD_INSTALLMENT_PLAN(HttpStatus.BAD_REQUEST, "할부 개월 정보가 잘못되었습니다."),
    NOT_SUPPORTED_MONTHLY_INSTALLMENT_PLAN(HttpStatus.BAD_REQUEST, "할부가 지원되지 않는 카드입니다."),
    EXCEED_MAX_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "하루 결제 가능 금액을 초과했습니다."),
    INVALID_AUTHORIZE_AUTH(HttpStatus.BAD_REQUEST, "결제 서버에 오류가 발생했습니다."),
    INVALID_CARD_LOST_OR_STOLEN(HttpStatus.BAD_REQUEST, "분실 또는 도난 카드입니다."),
    RESTRICTED_TRANSFER_ACCOUNT(HttpStatus.BAD_REQUEST, "계좌는 등록 후 12시간 뒤부터 결제할 수 있습니다."),
    INVALID_CARD_NUMBER(HttpStatus.BAD_REQUEST, "카드번호를 다시 확인해주세요."),
    INVALID_ACCOUNT_INFO_RE_REGISTER(HttpStatus.BAD_REQUEST, "유효하지 않은 계좌입니다. 계좌 재등록 후 시도해주세요."),
    INVALID_UNREGISTERED_SUBMALL(HttpStatus.BAD_REQUEST, "결제 서버에 오류가 발생했습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "결제 비밀번호가 일치하지 않습니다."),
    INCORRECT_BASIC_AUTH_FORMAT(HttpStatus.BAD_REQUEST, "결제 서버에 오류가 발생했습니다."),

    // 401
    UNAUTHORIZED_KEY(HttpStatus.UNAUTHORIZED, "결제 서버에 오류가 발생했습니다."),

    // 403
    REJECT_ACCOUNT_PAYMENT(HttpStatus.FORBIDDEN, "잔액부족으로 결제에 실패했습니다."),
    REJECT_CARD_PAYMENT(HttpStatus.FORBIDDEN, "한도초과 혹은 잔액부족으로 결제에 실패했습니다."),
    REJECT_CARD_COMPANY(HttpStatus.FORBIDDEN, "결제 승인이 거절되었습니다."),
    FORBIDDEN_REQUEST(HttpStatus.FORBIDDEN, "허용되지 않은 요청입니다."),
    REJECT_TOSSPAY_INVALID_ACCOUNT(HttpStatus.FORBIDDEN, "출금이체 등록이 되어 있지 않아요. 계좌를 다시 등록해 주세요."),
    EXCEED_MAX_AUTH_COUNT(HttpStatus.FORBIDDEN, "최대 인증 횟수를 초과했습니다. 카드사로 문의해주세요."),
    EXCEED_MAX_ONE_DAY_AMOUNT(HttpStatus.FORBIDDEN, "일일 한도를 초과했습니다."),
    NOT_AVAILABLE_BANK(HttpStatus.FORBIDDEN, "은행 서비스 시간이 아닙니다."),
    FDS_ERROR(HttpStatus.FORBIDDEN, "위험 거래가 감지되어 결제가 제한됩니다. 문자 링크로 본인인증 후 이용해주세요."),

    // 404
    NOT_FOUND_TERMINAL_ID(HttpStatus.NOT_FOUND, "단말기 정보가 없습니다."),
    NOT_FOUND_PAYMENT(HttpStatus.NOT_FOUND, "존재하지 않는 결제 정보입니다."),
    NOT_FOUND_PAYMENT_SESSION(HttpStatus.NOT_FOUND, "결제 시간이 만료되어 결제 진행 데이터가 존재하지 않습니다."),
    NOT_REGISTERED_BUSINESS(HttpStatus.NOT_FOUND, "등록되지 않은 사업자 번호입니다."),

    // 500
    CARD_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "카드사에서 오류가 발생했습니다."),
    FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING(HttpStatus.INTERNAL_SERVER_ERROR, "결제가 완료되지 않았어요. 다시 시도해주세요."),
    FAILED_INTERNAL_SYSTEM_PROCESSING(HttpStatus.INTERNAL_SERVER_ERROR, "내부 시스템 처리 작업이 실패했습니다. 잠시 후 다시 시도해주세요."),
    UNKNOWN_PAYMENT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "결제에 실패했어요. 반복되면 카드사로 문의해주세요."),

    // 결제 취소 API 에러 코드
    // 400
    ALREADY_CANCELED_PAYMENT(HttpStatus.BAD_REQUEST, "이미 취소된 결제입니다."),
    INVALID_REFUND_ACCOUNT_INFO(HttpStatus.BAD_REQUEST, "환불 계좌번호와 예금주명이 일치하지 않습니다."),
    EXCEED_CANCEL_AMOUNT_DISCOUNT_AMOUNT(HttpStatus.BAD_REQUEST, "즉시할인금액보다 적은 금액은 부분취소가 불가능합니다."),
    INVALID_REFUND_ACCOUNT_NUMBER(HttpStatus.BAD_REQUEST, "잘못된 환불 계좌번호입니다."),
    INVALID_BANK(HttpStatus.BAD_REQUEST, "유효하지 않은 은행입니다."),
    NOT_MATCHES_REFUNDABLE_AMOUNT(HttpStatus.BAD_REQUEST, "작앵 결과가 일치하지 않습니다."),
    REFUND_REJECTED(HttpStatus.BAD_REQUEST, "환불이 거절됐습니다. 결제사에 문의부탁드립니다."),
    ALREADY_REFUND_PAYMENT(HttpStatus.BAD_REQUEST, "이미 환불된 결제입니다."),
    FORBIDDEN_BANK_REFUND_REQUEST(HttpStatus.BAD_REQUEST, "고객 계좌가 입금이 되지 않는 상태입니다."),

    // 403
    NOT_CANCELABLE_AMOUNT(HttpStatus.FORBIDDEN, "취소 할 수 없는 금액입니다."),
    FORBIDDEN_CONSECUTIVE_REQUEST(HttpStatus.FORBIDDEN, "반복적인 요청은 허용되지 않습니다. 잠시 후 다시 시도해주세요."),
    NOT_CANCELABLE_PAYMENT(HttpStatus.FORBIDDEN, "취소 할 수 없는 결제 입니다."),
    EXCEED_MAX_REFUND_DUE(HttpStatus.FORBIDDEN, "환불 가능한 기간이 지났습니다."),
    NOT_ALLOWED_PARTIAL_REFUND_WAITING_DEPOSIT(HttpStatus.FORBIDDEN, "입금 대기중인 결제는 부분 환불이 불가능합니다."),
    NOT_ALLOWED_PARTIAL_REFUND(HttpStatus.FORBIDDEN, "에스크로 주문, 현금 카드 결제일때는 부분 환불이 불가합니다. 이외 다른 결제 수단에서 부분취소가 되지 않을 때는 토스페이머츤에 문의해 주세요."),
    NOT_CANCELABLE_PAYMENT_FOR_DORMANT_USER(HttpStatus.FORBIDDEN, "휴먼 처리된 회원의 결제는 취소할 수 없습니다."),

    // 500 Internal Server Error
    FAILED_INTERNAL_SYSETM_PROCESSING(HttpStatus.INTERNAL_SERVER_ERROR, "내부 시스템 처리 작업이 실패했습니다. 잠시 후 다시 시도해주세요."),
    FAILD_REFUND_PROCESS(HttpStatus.INTERNAL_SERVER_ERROR, "은행 응답시간 지연이나 일시적인 오류로 환불요청에 실패했습니다."),
    FAILD_METHOD_HANDLING_CANCEL(HttpStatus.INTERNAL_SERVER_ERROR, "취소 중 결제 시 사용한 결제 수단 처리과정에서 일시적인 오류가 발생했습니다."),
    FAILD_PARTIAL_REFUND(HttpStatus.INTERNAL_SERVER_ERROR, "은행 점검, 해약 계좌 등의 사유로 부분 환불이 실패했습니다."),
    COMMON_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    FAILED_PAYMENT_INTERNAL_SYSETM_PROCESSING(HttpStatus.INTERNAL_SERVER_ERROR, "결제가 완료되지 않았어요. 다시 시도해주세요."),
    FAILED_CLOSED_ACCOUNT_REFUND(HttpStatus.INTERNAL_SERVER_ERROR, "해약된 계좌로 인해 환불이 실패했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    TossPaymentErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    private static final Map<String, TossPaymentErrorCode> ERROR_CODE_MAP =
        Stream.of(values()).collect(Collectors.toMap(TossPaymentErrorCode::getCode, e -> e));

    public static TossPaymentErrorCode fromCode(String code) {
        return ERROR_CODE_MAP.getOrDefault(code, UNKNOWN_ERROR);
    }

    public String getCode() {
        return this.name();
    }
}
