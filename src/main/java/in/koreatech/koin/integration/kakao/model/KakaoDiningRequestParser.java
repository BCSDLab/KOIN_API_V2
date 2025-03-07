package in.koreatech.koin.integration.kakao.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import in.koreatech.koin.domain.dining.exception.DiningTypeNotFoundException;
import in.koreatech.koin.domain.dining.model.DiningType;
import in.koreatech.koin.integration.kakao.dto.KakaoDiningRequest;
import in.koreatech.koin.integration.kakao.exception.KakaoApiException;

public class KakaoDiningRequestParser implements KakaoSkillRequestParser<String, KakaoDiningRequest> {

    @Override
    public KakaoDiningRequest parse(String request) {
        try {
            JsonElement jsonElement = JsonParser.parseString(request);
            JsonElement action = jsonElement.getAsJsonObject().get("action");
            JsonElement params = action.getAsJsonObject().get("params");
            String diningTime = params.getAsJsonObject().get("dining_time").getAsString();
            return new KakaoDiningRequest(DiningType.from(diningTime));
        } catch (DiningTypeNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new KakaoApiException("잘못된 API 요청 형식입니다.", request);
        }
    }
}
