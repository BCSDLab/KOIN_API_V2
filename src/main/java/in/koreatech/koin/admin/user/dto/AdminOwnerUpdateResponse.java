package in.koreatech.koin.admin.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.owner.model.Owner;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminOwnerUpdateResponse (
    @Schema(description = "사업자 등록 번호", example = "012-34-56789", requiredMode = NOT_REQUIRED)
    String companyRegistrationNumber,

    @Schema(description = "상점 수정 권한", example = "false", requiredMode = NOT_REQUIRED)
    Boolean grantShop,

    @Schema(description = "이벤트 수정 권한", example = "false", requiredMode = NOT_REQUIRED)
    Boolean grantEvent,

    @Schema(description = "사장님 전화번호", example = "01012345678", requiredMode = NOT_REQUIRED)
    String phoneNumber
) {
    public static AdminOwnerUpdateResponse from(Owner owner) {
        return new AdminOwnerUpdateResponse(
            owner.getCompanyRegistrationNumber(),
            owner.isGrantShop(),
            owner.isGrantEvent(),
            owner.getAccount()
        );
    }
}
