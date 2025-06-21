package in.koreatech.koin.domain.order.address.exception;

import java.util.Arrays;
import java.util.Objects;

import org.springframework.http.HttpStatus;

import in.koreatech.koin._common.exception.errorcode.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AddressErrorCode implements BaseErrorCode {

    // 400 (사용자 입력 오류)
    KEYWORD_NOT_PROVIDED(HttpStatus.BAD_REQUEST, "E0005", "검색어를 입력해주세요."),
    KEYWORD_TOO_EXTENSIVE(HttpStatus.BAD_REQUEST, "E0006", "주소를 상세히 입력해 주세요"),
    KEYWORD_TOO_SHORT(HttpStatus.BAD_REQUEST, "E0008", "검색어는 두 글자 이상 입력해야 합니다."),
    KEYWORD_ONLY_NUMBER(HttpStatus.BAD_REQUEST, "E0009", "검색어는 문자와 숫자 같이 입력되어야 합니다."),
    KEYWORD_TOO_LONG(HttpStatus.BAD_REQUEST, "E0010", "검색어가 너무 깁니다. (한글 40자 이하)"),
    INVALID_KEYWORD(HttpStatus.BAD_REQUEST, "E0013", "검색어에 사용할 수 없는 특수문자(%,=,<,[,])가 포함되어 있습니다."),
    SEARCH_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "E0015", "검색 결과가 너무 많습니다."),

    // 500 (서버 오류 또는 외부 API 호출 문제)
    SYSTEM_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "-999", "도로명주소 API 시스템에 에러가 발생했습니다. 잠시 후 다시 시도해주세요."),
    INVALID_API_KEY(HttpStatus.INTERNAL_SERVER_ERROR, "E0001", "서비스 설정에 오류가 발생했습니다."),
    API_KEY_EXPIRED(HttpStatus.INTERNAL_SERVER_ERROR, "E0014", "서비스 설정에 오류가 발생했습니다."),
    EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "EXTERNAL_API_ERROR", "주소 정보를 가져오는 중 오류가 발생했습니다.");

    private final HttpStatus httpStatusCode;
    private final String externalApiCode;
    @Getter
    private final String message;

    @Override
    public String getHttpCode() {
        return httpStatusCode.toString();
    }

    @Override
    public Integer getHttpIntegerCode() {
        return httpStatusCode.value();
    }

    public static AddressErrorCode from(String externalApiCode) {
        return Arrays.stream(AddressErrorCode.values())
            .filter(code -> Objects.equals(code.externalApiCode, externalApiCode))
            .findFirst()
            .orElse(EXTERNAL_API_ERROR);
    }
}
