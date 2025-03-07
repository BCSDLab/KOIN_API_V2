package in.koreatech.koin.integration.kakao.dto;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
public record KakaoBusRequest(
    String depart,
    String arrival
) {

}
