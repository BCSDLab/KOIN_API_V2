package in.koreatech.koin._common.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApiResponseCode {

    /**
     * 2xx Success (성공)
     */
    OK(HttpStatus.OK, "요청이 성공적으로 처리되었습니다."),
    CREATED(HttpStatus.CREATED, "요청이 성공적으로 처리되어 리소스가 생성되었습니다."),
    NO_CONTENT(HttpStatus.NO_CONTENT, "요청이 성공적으로 처리되었으나 반환할 내용이 없습니다."),

    /**
     * 400 Bad Request (잘못된 요청)
     */
    ILLEGAL_ARGUMENT(HttpStatus.BAD_REQUEST, "잘못된 인자가 전달되었습니다."),
    ILLEGAL_STATE(HttpStatus.BAD_REQUEST, "잘못된 상태로 요청이 들어왔습니다."),
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "잘못된 입력값이 포함되어 있습니다."),
    INVALID_DATE_TIME(HttpStatus.BAD_REQUEST, "잘못된 날짜 형식입니다."),
    INVALID_GENDER_INDEX(HttpStatus.BAD_REQUEST, "올바르지 않은 성별 인덱스입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "올바르지 않은 인증 토큰입니다."),
    INVALID_DELIVERY_AREA(HttpStatus.BAD_REQUEST, "배달이 불가능한 지역이에요."),
    NOT_MATCHED_EMAIL(HttpStatus.BAD_REQUEST, "이메일이 일치하지 않습니다."),
    NOT_MATCHED_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "전화번호가 일치하지 않습니다."),
    NOT_MATCHED_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    NOT_MATCHED_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "인증 번호가 일치하지 않습니다."),
    NOT_MATCHED_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "리프레시 토큰이 일치하지 않습니다."),
    NOT_READABLE_HTTP_MESSAGE(HttpStatus.BAD_REQUEST, "잘못된 입력 형식이거나, 값이 허용된 범위를 초과했습니다."),
    UNSUPPORTED_OPERATION(HttpStatus.BAD_REQUEST, "지원하지 않는 API 입니다."),
    INVALID_RECRUITMENT_PERIOD(HttpStatus.BAD_REQUEST, "모집 마감일은 모집 시작일 이후여야 합니다."),
    MUST_BE_NULL_RECRUITMENT_PERIOD(HttpStatus.BAD_REQUEST, "상시 모집일 경우, 모집 시작일과 종료일은 입력하면 안 됩니다."),
    REQUIRED_RECRUITMENT_PERIOD(HttpStatus.BAD_REQUEST, "상시 모집이 아닌 경우, 모집 시작일과 종료일은 필수입니다."),
    NOT_MATCHED_CLUB_AND_EVENT(HttpStatus.BAD_REQUEST, "해당 동아리의 이벤트가 아닙니다."),
    NOT_ALLOWED_RECRUITING_SORT_TYPE(HttpStatus.BAD_REQUEST, "해당 정렬 방식은 모집 중일 때만 사용할 수 있습니다."),
    INVALID_CLUB_EVENT_PERIOD(HttpStatus.BAD_REQUEST,"행사 마감일은 행사 시작일 이후여야 합니다."),
    INVALID_CLUB_EVENT_TYPE(HttpStatus.BAD_REQUEST, "올바르지 않은 동아리 행사 타입입니다."),
    SHOP_NOT_DELIVERABLE(HttpStatus.BAD_REQUEST, "배달 가능한 상점이 아닙니다."),
    SHOP_NOT_TAKEOUT_AVAILABLE(HttpStatus.BAD_REQUEST, "포장 가능한 상점이 아닙니다."),
    ADDRESS_KEYWORD_NOT_PROVIDED(HttpStatus.BAD_REQUEST, "검색어를 입력해주세요."),
    ADDRESS_KEYWORD_TOO_EXTENSIVE(HttpStatus.BAD_REQUEST, "주소를 상세히 입력해 주세요"),
    ADDRESS_KEYWORD_TOO_SHORT(HttpStatus.BAD_REQUEST, "검색어는 두 글자 이상 입력해야 합니다."),
    ADDRESS_KEYWORD_ONLY_NUMBER(HttpStatus.BAD_REQUEST, "검색어는 문자와 숫자 같이 입력되어야 합니다."),
    ADDRESS_KEYWORD_TOO_LONG(HttpStatus.BAD_REQUEST, "검색어가 너무 깁니다. (한글 40자 이하)"),
    ADDRESS_KEYWORD_INVALID_SYMBOLS(HttpStatus.BAD_REQUEST, "검색어에 사용할 수 없는 특수문자(%,=,<,[,])가 포함되어 있습니다."),
    ADDRESS_SEARCH_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "검색 결과가 너무 많습니다."),
    DIFFERENT_SHOP_ITEM_IN_CART(HttpStatus.BAD_REQUEST, "장바구니에는 동일한 상점의 상품만 담을 수 있습니다."),
    MENU_SOLD_OUT(HttpStatus.BAD_REQUEST, "상품이 매진되었습니다"),
    SHOP_CLOSED(HttpStatus.BAD_REQUEST, "상점의 영업시간이 아닙니다."),
    INVALID_MENU_IN_SHOP(HttpStatus.BAD_REQUEST, "선택한 메뉴는 해당 상점에 속해있지 않습니다"),
    INVALID_OPTION_IN_GROUP(HttpStatus.BAD_REQUEST, "선택한 옵션이 해당 옵션 그룹에 속해있지 않습니다."),
    INVALID_CART_ITEM_QUANTITY(HttpStatus.BAD_REQUEST, "유효하지 않은 수량입니다."),
    REQUIRED_OPTION_GROUP_MISSING(HttpStatus.BAD_REQUEST, "필수 옵션 그룹을 선택하지 않았습니다."),
    MIN_SELECTION_NOT_MET(HttpStatus.BAD_REQUEST, "옵션 그룹의 최소 선택 개수를 만족하지 못했습니다."),
    MAX_SELECTION_EXCEEDED(HttpStatus.BAD_REQUEST, "옵션 그룹의 최대 선택 개수를 초과했습니다."),
    ORDER_AMOUNT_BELOW_MINIMUM(HttpStatus.BAD_REQUEST, "최소 주문 금액을 충족하지 않습니다."),

    /**
     * 401 Unauthorized (인증 필요)
     */
    WITHDRAWN_USER(HttpStatus.UNAUTHORIZED, "탈퇴한 계정입니다."),

    /**
     * 403 Forbidden (인가 필요)
     */
    FORBIDDEN_USER_TYPE(HttpStatus.FORBIDDEN, "인가되지 않은 유저 타입입니다."),
    FORBIDDEN_OWNER(HttpStatus.FORBIDDEN, "관리자 인증 대기중입니다."),
    FORBIDDEN_STUDENT(HttpStatus.FORBIDDEN, "아우누리에서 인증메일을 확인해주세요."),
    FORBIDDEN_ADMIN(HttpStatus.FORBIDDEN, "PL 인증 대기중입니다."),
    FORBIDDEN_ACCOUNT(HttpStatus.FORBIDDEN, "유효하지 않은 계정입니다."),
    FORBIDDEN_VERIFICATION(HttpStatus.FORBIDDEN, "이메일/휴대폰 인증 후 다시 시도해주십시오."),

    /**
     * 404 Not Found (리소스를 찾을 수 없음)
     */
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "사용자가 존재하지 않습니다."),
    NOT_FOUND_REFRESH_TOKEN(HttpStatus.NOT_FOUND, "Refresh token이 존재하지 않습니다."),
    NOT_FOUND_RESET_TOKEN(HttpStatus.NOT_FOUND, "Reset token이 존재하지 않습니다."),
    NO_HANDLER_FOUND(HttpStatus.NOT_FOUND, "유효하지 않은 API 경로입니다."),
    NOT_FOUND_CLUB(HttpStatus.NOT_FOUND, "동아리가 존재하지 않습니다."),
    NOT_FOUND_CLUB_RECRUITMENT(HttpStatus.NOT_FOUND, "동아리 모집 공고가 존재하지 않습니다."),
    NOT_FOUND_CLUB_EVENT(HttpStatus.NOT_FOUND, "동아리 행사가 존재하지 않습니다."),
    NOT_FOUND_DELIVERY_ADDRESS(HttpStatus.NOT_FOUND, "주소가 존재하지 않습니다."),
    NOT_FOUND_ORDERABLE_SHOP(HttpStatus.NOT_FOUND, "상점이 존재하지 않습니다."),
    NOT_FOUND_ORDERABLE_SHOP_MENU(HttpStatus.NOT_FOUND, "메뉴가 존재하지 않습니다"),
    NOT_FOUND_ORDERABLE_SHOP_MENU_PRICE(HttpStatus.NOT_FOUND, "유효하지 않은 가격 ID 입니다."),
    NOT_FOUND_ORDERABLE_SHOP_MENU_OPTION(HttpStatus.NOT_FOUND, "유효하지 않은 옵션 ID 입니다."),
    NOT_FOUND_CART(HttpStatus.NOT_FOUND, "장바구니가 존재하지 않습니다"),
    NOT_FOUND_CART_ITEM(HttpStatus.NOT_FOUND, "장바구니에 담긴 상품이 존재하지 않습니다"),

    /**
     * 409 CONFLICT (중복 혹은 충돌)
     */
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "이미 존재하는 로그인 아이디입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    DUPLICATE_PHONE_NUMBER(HttpStatus.CONFLICT, "이미 존재하는 전화번호입니다."),
    REQUEST_TOO_FAST(HttpStatus.CONFLICT, "요청이 너무 빠릅니다. 다시 요청해주세요."),
    OPTIMISTIC_LOCKING_FAILURE(HttpStatus.CONFLICT, "이미 처리된 요청입니다."),
    DUPLICATE_CLUB_RECRUITMENT(HttpStatus.CONFLICT, "동아리 공고가 이미 존재합니다."),

    /**
     * 429 Too Many Requests (요청량 초과)
     */
    TOO_MANY_REQUESTS_VERIFICATION(HttpStatus.TOO_MANY_REQUESTS, "하루 인증 횟수를 초과했습니다."),

    /**
     * 500 Internal Server Error (서버 오류)
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 오류가 발생했습니다."),
    CLIENT_ABORTED(HttpStatus.INTERNAL_SERVER_ERROR, "클라이언트에 의해 연결이 중단되었습니다."),
    EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "외부 API 호출 중 오류가 발생했습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String message;

    public String getCode() {
        return this.name();
    }
}
