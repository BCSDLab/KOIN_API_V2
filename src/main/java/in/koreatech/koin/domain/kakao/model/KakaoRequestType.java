package in.koreatech.koin.domain.kakao.model;

public enum KakaoRequestType {
    DINING(new KakaoDiningRequestParser()),
    ;

    private final KakaoSkillRequestParser<String, ?> parser;

    <R> KakaoRequestType(KakaoSkillRequestParser<String, R> parser) {
        this.parser = parser;
    }

    public <R> R parse(String request) {
        return (R)parser.parse(request);
    }
}
