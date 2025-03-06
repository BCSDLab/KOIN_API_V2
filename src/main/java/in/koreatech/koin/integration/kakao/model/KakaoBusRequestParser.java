package in.koreatech.koin.integration.kakao.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import in.koreatech.koin.integration.kakao.dto.KakaoBusRequest;
import in.koreatech.koin.integration.kakao.exception.KakaoApiException;

public class KakaoBusRequestParser implements KakaoSkillRequestParser<String, KakaoBusRequest> {

    @Override
    public KakaoBusRequest parse(String request) throws KakaoApiException {
        try {
            JsonElement jsonElement = JsonParser.parseString(request);
            JsonElement action = jsonElement.getAsJsonObject().get("action");
            JsonElement params = action.getAsJsonObject().get("params");
            String depart = params.getAsJsonObject().get("depart").getAsString();
            String arrival = params.getAsJsonObject().get("arrival").getAsString();
            return new KakaoBusRequest(depart, arrival);
        } catch (Exception e) {
            throw new KakaoApiException("잘못된 API 요청 형식입니다.", request);
        }
    }
}
