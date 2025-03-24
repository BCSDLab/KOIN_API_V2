package in.koreatech.koin.domain.banner.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(SnakeCaseStrategy.class)
public record ModifyBannerRequest(

) {
}
