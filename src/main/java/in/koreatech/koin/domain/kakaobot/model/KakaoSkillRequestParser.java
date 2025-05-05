package in.koreatech.koin.domain.kakaobot.model;

import in.koreatech.koin.domain.kakaobot.exception.KakaoApiException;

public interface KakaoSkillRequestParser<T, R> {

    R parse(T request) throws KakaoApiException;
}
