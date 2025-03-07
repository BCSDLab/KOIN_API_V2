package in.koreatech.koin.admin.benefit.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin._common.validation.NotBlankElement;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminModifyBenefitShopsRequest(
    @NotNull(message = "혜택문구 변경정보 리스트는 필수입니다.")
    @NotBlankElement(message = "혜택문구 변경정보 리스트는 빈 요소가 존재할 수 없습니다.")
    List<InnerBenefitShopsRequest> modifyDetails
) {

    @JsonNaming(SnakeCaseStrategy.class)
    public record InnerBenefitShopsRequest(
        @Schema(description = "상점혜택 매핑id", example = "2", requiredMode = REQUIRED)
        @NotNull(message = "상점혜택 매핑id는 필수입니다.")
        Integer shopBenefitMapId,

        @Schema(description = "혜택 미리보기 문구", example = "4인 이상 픽업서비스", requiredMode = REQUIRED)
        @NotBlank(message = "혜택 미리보기 문구는 필수입니다.")
        @Size(min = 2, max = 15, message = "혜택 미리보기 문구는 최소 2자 최대 15자입니다.")
        String detail
    ) {
    }
}
