package in.koreatech.koin.domain.kakao.exception;

import lombok.Getter;

@Getter
public class KakaoSkillFailedException extends IllegalStateException {

    private static final String DEFAULT_MESSAGE = "카카오 챗봇 응답 생성과정에서 문제가 생겼습니다.";
    private final String detail;

    public KakaoSkillFailedException(String message) {
        super(message);
        this.detail = null;
    }

    public KakaoSkillFailedException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static KakaoSkillFailedException withDetail(String detail) {
        return new KakaoSkillFailedException(DEFAULT_MESSAGE, detail);
    }
}
