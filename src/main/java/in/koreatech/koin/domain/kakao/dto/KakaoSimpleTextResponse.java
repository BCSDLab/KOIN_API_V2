package in.koreatech.koin.domain.kakao.dto;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <a href="https://docs.kakaoi.ai/skill/api_reference/">Bot-Skill 서버 연동 API</a>
 * @param response
 */
public record KakaoSimpleTextResponse(
    InnerKakaoDiningResponse response
) {

    public record InnerKakaoDiningResponse(
        String version,
        InnerKakaoResponses template
    ) {

    }

    private record InnerKakaoResponses(
        List<InnerKakaoOutPut> outputs,
        List<String> quickReplies
    ) {

    }

    private record InnerKakaoOutPut(
        Map<String, String> simpleText
    ) {

    }

    public static KakaoSimpleTextResponse from(String diningResponse) {
        return new KakaoSimpleTextResponse(
            new InnerKakaoDiningResponse(
                "2.0",
                new InnerKakaoResponses(
                    List.of(
                        new InnerKakaoOutPut(Map.of("text", diningResponse))
                    ),
                    Collections.emptyList()
                )
            )
        );
    }
}
