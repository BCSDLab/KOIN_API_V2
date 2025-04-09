package in.koreatech.koin.domain.user.dto.verification;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record VerificationCountResponse(
    @Schema(description = "전화번호 또는 코리아텍 이메일", example = "01000000000 or test@koreatech.ac.kr", requiredMode = REQUIRED)
    String target,

    @Schema(description = "하루 최대 인증 가능 횟수", example = "5", requiredMode = REQUIRED)
    int totalCount,

    @Schema(description = "남은 인증 가능 횟수", example = "3",requiredMode = REQUIRED)
    int remainingCount,

    @Schema(description = "현재 사용한 인증 횟수", example = "2", requiredMode = REQUIRED)
    int currentCount
) {

    public static VerificationCountResponse of(String target, int totalCount, int remainingCount, int currentCount) {
        return new VerificationCountResponse(target, totalCount, remainingCount, currentCount);
    }
}
