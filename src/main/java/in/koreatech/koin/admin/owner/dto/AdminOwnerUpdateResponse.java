package in.koreatech.koin.admin.owner.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.owner.model.Owner;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminOwnerUpdateResponse (
    @Schema(description = "사업자 등록 번호", example = "012-34-56789", requiredMode = NOT_REQUIRED)
    String companyRegistrationNumber,

    @Schema(description = "이메일 주소", example = "koin123@koreatech.ac.kr", requiredMode = NOT_REQUIRED)
    String email,

    @Schema(description = "성별(남:0, 여:1)", example = "1", requiredMode = NOT_REQUIRED)
    Integer gender,

    @Schema(description = "상점 수정 권한", example = "false", requiredMode = NOT_REQUIRED)
    Boolean grantShop,

    @Schema(description = "이벤트 수정 권한", example = "false", requiredMode = NOT_REQUIRED)
    Boolean grantEvent,

    @Schema(description = "이름", example = "최준호", requiredMode = NOT_REQUIRED)
    String name,

    @Schema(description = "닉네임", example = "juno", requiredMode = NOT_REQUIRED)
    String nickname,

    @Schema(description = "사장님 전화번호", example = "01012345678", requiredMode = NOT_REQUIRED)
    String phoneNumber
) {
    public static AdminOwnerUpdateResponse from(Owner owner) {
        return new AdminOwnerUpdateResponse(
            owner.getCompanyRegistrationNumber(),
            owner.getUser().getEmail(),
            owner.getUser().getGender() == null ? null : owner.getUser().getGender().ordinal(),
            owner.isGrantShop(),
            owner.isGrantEvent(),
            owner.getUser().getName(),
            owner.getUser().getNickname(),
            owner.getAccount()
            );
    }
}
