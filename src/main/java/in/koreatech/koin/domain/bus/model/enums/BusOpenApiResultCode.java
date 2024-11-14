package in.koreatech.koin.domain.bus.model.enums;

import java.util.Arrays;
import java.util.Optional;

import com.google.gson.JsonObject;

import in.koreatech.koin.domain.bus.exception.BusOpenApiException;
import lombok.Getter;

@Getter
public enum BusOpenApiResultCode {
    SERVICE_DISPOSE("12", "버스도착정보 공공 API 서비스가 폐기되었습니다."),
    SERVICE_ACCESS_DENIED("20", "버스도착정보 공공 API 서비스가 접근 거부 상태입니다."),
    SERVICE_REQUEST_OVER("22", "버스도착정보 공공 API 서비스의 요청 제한 횟수가 초과되었습니다."),
    KEY_UNREGISTERED("30", "등록되지 않은 버스도착정보 공공 API 서비스 키입니다."),
    SERVICE_KEY_EXPIRED("31", "버스도착정보 공공 API 서비스 키의 활용 기간이 만료되었습니다."),
    SERVICE_SUCCESS("00", "NORMAL SERVICE."),
    ;

    private final String code;
    private final String message;

    BusOpenApiResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static void validateResponse(JsonObject response) {
        String resultCode = response.get("header").getAsJsonObject().get("resultCode").getAsString();

        String errorMessage = "";

        if (!resultCode.equals(SERVICE_SUCCESS.code)) {
            Optional<BusOpenApiResultCode> code = Arrays.stream(BusOpenApiResultCode.values())
                .filter(busOpenApiResultCode -> busOpenApiResultCode.code.equals(resultCode))
                .findFirst();

            if (code.isPresent()) {
                errorMessage = code.get().message;
            }

            String resultMessage = response.get("header").getAsJsonObject().get("resultMsg").getAsString();
            throw BusOpenApiException.withDetail(errorMessage + " resultMsg: " + resultMessage);
        }
    }
}
