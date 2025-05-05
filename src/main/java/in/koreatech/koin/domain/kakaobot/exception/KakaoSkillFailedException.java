package in.koreatech.koin.domain.kakaobot.exception;

import in.koreatech.koin._common.exception.custom.KoinIllegalStateException;

public class KakaoSkillFailedException extends KoinIllegalStateException {

    private static final String DEFAULT_MESSAGE = "카카오 챗봇 응답 생성과정에서 문제가 생겼습니다.";

    public KakaoSkillFailedException(String message) {
        super(message);
    }

    public KakaoSkillFailedException(String message, String detail) {
        super(message, detail);
    }

    public static KakaoSkillFailedException withDetail(String detail) {
        return new KakaoSkillFailedException(DEFAULT_MESSAGE, detail);
    }
}
