package in.koreatech.koin.domain.kakao.dto;

import in.koreatech.koin.domain.dining.model.DiningType;
import io.swagger.v3.oas.annotations.Hidden;

@Hidden
public record KakaoDiningRequest(
    DiningType diningType
) {

}
