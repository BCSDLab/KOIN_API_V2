package in.koreatech.koin.domain.dining.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record DiningReportDecisionRequest(
    @Schema(description = "처리자 감사 정보. 서비스 토큰으로 인증하며 actor는 감사 기록에만 사용합니다.", requiredMode = REQUIRED)
    @Valid @NotNull(message = "처리자 정보는 필수입니다.") DiningReportActor actor
) {
}
