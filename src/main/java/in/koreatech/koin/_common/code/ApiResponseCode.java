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
    INVALID_REQUEST_PAYLOAD(HttpStatus.BAD_REQUEST, "잘못된 입력값이 포함되어 있습니다."),
    INVALID_DATE_TIME(HttpStatus.BAD_REQUEST, "잘못된 날짜 형식입니다."),
    INVALID_GENDER_INDEX(HttpStatus.BAD_REQUEST, "올바르지 않은 성별 인덱스입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "올바르지 않은 인증 토큰입니다."),
    NOT_MATCHED_EMAIL(HttpStatus.BAD_REQUEST, "이메일이 일치하지 않습니다."),
    NOT_MATCHED_PHONE_NUMBER(HttpStatus.BAD_REQUEST, "전화번호가 일치하지 않습니다."),
    NOT_MATCHED_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    NOT_MATCHED_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "인증 번호가 일치하지 않습니다."),
    NOT_MATCHED_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "리프레시 토큰이 일치하지 않습니다."),
    NOT_READABLE_HTTP_MESSAGE(HttpStatus.BAD_REQUEST, "잘못된 입력 형식이거나, 값이 허용된 범위를 초과했습니다."),
    UNSUPPORTED_OPERATION(HttpStatus.BAD_REQUEST, "지원하지 않는 API 입니다."),

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

    /**
     * 409 CONFLICT (중복 혹은 충돌)
     */
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "이미 존재하는 로그인 아이디입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    DUPLICATE_PHONE_NUMBER(HttpStatus.CONFLICT, "이미 존재하는 전화번호입니다."),
    REQUEST_TOO_FAST(HttpStatus.CONFLICT, "요청이 너무 빠르게 발생했습니다."),
    OPTIMISTIC_LOCKING_FAILURE(HttpStatus.CONFLICT, "이미 처리된 요청입니다."),

    /**
     * 429 Too Many Requests (요청량 초과)
     */
    TOO_MANY_REQUESTS_VERIFICATION(HttpStatus.TOO_MANY_REQUESTS, "하루 인증 횟수를 초과했습니다."),

    /**
     *
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 오류가 발생했습니다."),
    CLIENT_ABORTED(HttpStatus.INTERNAL_SERVER_ERROR, "클라이언트에 의해 연결이 중단되었습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    public String getCode() {
        return this.name();
    }
}
