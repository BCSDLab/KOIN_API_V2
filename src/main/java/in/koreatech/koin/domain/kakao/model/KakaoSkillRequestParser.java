package in.koreatech.koin.domain.kakao.model;

import in.koreatech.koin.domain.kakao.exception.KakaoApiException;

public interface KakaoSkillRequestParser<T, R> {

    R parse(T request) throws KakaoApiException;
}
