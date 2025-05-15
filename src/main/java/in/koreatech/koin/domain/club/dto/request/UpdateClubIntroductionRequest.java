package in.koreatech.koin.domain.club.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

@JsonNaming(value = SnakeCaseStrategy.class)
public record UpdateClubIntroductionRequest(
    @Schema(description = "동아리 상세 소개", example = "BCSD는 스타트업형 IT 동아리로 KOIN을 운영하고 있습니다", requiredMode = REQUIRED)
    @NotEmpty(message = "동아리 상세 소개를 입력해주세요.")
    String introduction
) {
}
