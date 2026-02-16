package in.koreatech.koin.domain.callvan.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.callvan.model.enums.CallvanReportReasonCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record CallvanUserReportCreateRequest(
    @Schema(description = "신고 대상 사용자 ID", example = "2", requiredMode = REQUIRED)
    @NotNull(message = "신고 대상 사용자는 필수입니다.")
    Integer reportedUserId,

    @Schema(description = "신고 사유 목록", requiredMode = REQUIRED)
    @NotEmpty(message = "신고 사유는 1개 이상 선택해야 합니다.")
    @Valid
    List<CallvanUserReportReasonRequest> reasons
) {

    @JsonNaming(SnakeCaseStrategy.class)
    public record CallvanUserReportReasonRequest(
        @Schema(description = "신고 사유 코드", example = "NON_PAYMENT", requiredMode = REQUIRED)
        @NotNull(message = "신고 사유 코드는 필수입니다.")
        CallvanReportReasonCode reasonCode,

        @Schema(description = "기타(OTHER) 선택 시 상세 입력", example = "욕설 및 협박성 발언")
        @Size(max = 150, message = "기타 사유는 150자 이하여야 합니다.")
        String customText
    ) {
    }
}
