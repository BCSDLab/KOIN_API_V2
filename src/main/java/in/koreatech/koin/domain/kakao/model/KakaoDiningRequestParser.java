package in.koreatech.koin.domain.kakao.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import in.koreatech.koin.domain.kakao.exception.KakaoApiException;

public class KakaoDiningRequestParser implements KakaoSkillRequestParser<String, String> {

    @Override
    public String parse(String request) {
        try {
            JsonElement jsonElement = JsonParser.parseString(request);
            JsonElement action = jsonElement.getAsJsonObject().get("action");
            JsonElement params = action.getAsJsonObject().get("params");
            return params.getAsJsonObject().get("dining_time").getAsString();
        } catch (Exception e) {
            throw new KakaoApiException("잘못된 API 요청 형식입니다.", request);
        }
    }
}
