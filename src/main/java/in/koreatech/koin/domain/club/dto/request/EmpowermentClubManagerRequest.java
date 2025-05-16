package in.koreatech.koin.domain.club.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

public record EmpowermentClubManagerRequest(
    @Schema(description = "동아리 아이디", example = "1", requiredMode = REQUIRED)
    Integer clubId,

    @Schema(description = "위임받는 매니저의 이메일", example = "example@koreatech.ac.kr", requiredMode = REQUIRED)
    @NotEmpty(message = "위임받는 사람의 이메일을 입력해주세요.")
    String changedManagerEmail
) {
}
