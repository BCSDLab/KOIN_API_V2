package in.koreatech.koin.domain.user.verification.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.user.verification.model.UserDailyVerificationCount;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record SendVerificationResponse(
    @Schema(description = "전화번호 또는 코리아텍 이메일", example = "01000000000 or test@koreatech.ac.kr", requiredMode = REQUIRED)
    String target,

    @Schema(description = "하루 최대 인증 가능 횟수", example = "5", requiredMode = REQUIRED)
    int totalCount,

    @Schema(description = "남은 인증 가능 횟수", example = "3", requiredMode = REQUIRED)
    int remainingCount,

    @Schema(description = "현재 사용한 인증 횟수", example = "2", requiredMode = REQUIRED)
    int currentCount
) {

    public static SendVerificationResponse from(UserDailyVerificationCount verificationCount) {
        int max = UserDailyVerificationCount.MAX_VERIFICATION_COUNT;
        int current = verificationCount.getVerificationCount();
        return new SendVerificationResponse(
            verificationCount.getId(),
            max,
            max - current,
            current
        );
    }
}
