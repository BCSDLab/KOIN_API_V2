package in.koreatech.koin.domain.kakao.config;

import java.util.function.UnaryOperator;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import in.koreatech.koin.domain.kakao.exception.KakaoApiException;
import lombok.Getter;

@Getter
public enum KakaoRequestType {
    DINING(request -> {
        try {
            JsonElement jsonElement = JsonParser.parseString(request);
            JsonElement action = jsonElement.getAsJsonObject().get("action");
            JsonElement params = action.getAsJsonObject().get("params");
            return params.getAsJsonObject().get("dining_time").getAsString();
        } catch (Exception e) {
            throw new KakaoApiException("잘못된 API 요청 형식입니다.", request);
        }
    }),
    ;

    private final UnaryOperator<String> parser;

    KakaoRequestType(UnaryOperator<String> parser) {
        this.parser = parser;
    }
}
