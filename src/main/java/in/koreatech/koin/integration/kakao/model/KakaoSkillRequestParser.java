package in.koreatech.koin.integration.kakao.model;

import in.koreatech.koin.integration.kakao.exception.KakaoApiException;

public interface KakaoSkillRequestParser<T, R> {

    R parse(T request) throws KakaoApiException;
}
