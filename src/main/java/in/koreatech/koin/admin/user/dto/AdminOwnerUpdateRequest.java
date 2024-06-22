package in.koreatech.koin.admin.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminOwnerUpdateRequest (
    @Schema(description = "사업자 등록 번호", example = "012-34-56789", requiredMode = NOT_REQUIRED)
    @Size(max = 12, message = "사업자 등록 번호는 12자 이하로 입력해주세요.")
    String companyRegistrationNumber,

    @Schema(description = "상점 수정 권한", example = "false", requiredMode = NOT_REQUIRED)
    Boolean grantShop,

    @Schema(description = "이벤트 수정 권한", example = "false", requiredMode = NOT_REQUIRED)
    Boolean grantEvent
) {

}
