package in.koreatech.koin.domain.kakaobot.dto;

import in.koreatech.koin.domain.dining.model.DiningType;
import io.swagger.v3.oas.annotations.Hidden;

@Hidden
public record KakaoDiningRequest(
    DiningType diningType
) {

}
