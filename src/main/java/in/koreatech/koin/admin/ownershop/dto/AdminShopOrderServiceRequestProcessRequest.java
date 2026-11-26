package in.koreatech.koin.admin.ownershop.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminShopOrderServiceRequestProcessRequest(
    @Schema(description = "신청 처리 유형", example = "APPROVE", requiredMode = REQUIRED)
    @NotNull(message = "신청 처리 유형은 필수입니다.")
    ProcessType processType,

    @Schema(description = "반려 시 필수인 사유", example = "제출 서류를 확인해 주세요.")
    @Size(max = 500, message = "반려 사유는 500자 이하여야 합니다.")
    String rejectionReason
) {

    @JsonIgnore
    @AssertTrue(message = "반려 시에만 반려 사유를 입력해야 합니다.")
    public boolean isRejectionReasonValid() {
        if (processType == null) {
            return true;
        }
        return switch (processType) {
            case APPROVE -> rejectionReason == null;
            case REJECT -> rejectionReason != null && !rejectionReason.isBlank();
        };
    }

    public enum ProcessType {
        APPROVE, REJECT
    }
}
