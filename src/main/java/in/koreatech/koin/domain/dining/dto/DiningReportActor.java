package in.koreatech.koin.domain.dining.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
@Schema(description = "삐봇 처리자 감사 정보. 서비스 토큰으로 인증하고 이 정보는 감사 기록에만 사용합니다.")
public record DiningReportActor(
    @Schema(description = "처리자의 Slack 워크스페이스 ID. 공백만 입력할 수 없습니다.", example = "T0123456789",
        minLength = 1, maxLength = 64, requiredMode = REQUIRED)
    @NotBlank @Size(max = 64) String workspaceId,
    @Schema(description = "처리자의 Slack 사용자 ID. 공백만 입력할 수 없습니다.", example = "U0123456789",
        minLength = 1, maxLength = 64, requiredMode = REQUIRED)
    @NotBlank @Size(max = 64) String userId,
    @Schema(description = "처리자 표시 이름. 공백만 입력할 수 없습니다.", example = "홍길동",
        minLength = 1, maxLength = 80, requiredMode = REQUIRED)
    @NotBlank @Size(max = 80) String displayName
) {
}
