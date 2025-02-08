package in.koreatech.koin.admin.owner.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminOwnerUpdateRequest (
    @Schema(description = "사업자 등록 번호", example = "012-34-56789", requiredMode = NOT_REQUIRED)
    @Size(max = 12, message = "사업자 등록 번호는 12자 이하로 입력해주세요.")
    String companyRegistrationNumber,

    @Schema(description = "이메일 주소", example = "koin123@koreatech.ac.kr", requiredMode = NOT_REQUIRED)
    @Size(max = 100, message = "이메일의 길이는 최대 100자입니다")
    String email,

    @Schema(description = "성별(남:0, 여:1)", example = "1", requiredMode = NOT_REQUIRED)
    Integer gender,

    @Schema(description = "상점 수정 권한", example = "false", requiredMode = NOT_REQUIRED)
    Boolean grantShop,

    @Schema(description = "이벤트 수정 권한", example = "false", requiredMode = NOT_REQUIRED)
    Boolean grantEvent,

    @Schema(description = "이름", example = "최준호", requiredMode = NOT_REQUIRED)
    @Size(max = 50, message = "이름의 길이는 최대 50자 입니다.")
    String name,

    @Schema(description = "닉네임", example = "juno", requiredMode = NOT_REQUIRED)
    @Size(max = 10, message = "닉네임은 10자 이내여야 합니다.")
    String nickname,

    @Schema(description = "사장님 전화번호", example = "01012345678", requiredMode = NOT_REQUIRED)
    @Size(max = 20, message = "휴대전화의 길이는 최대 20자 입니다")
    String phoneNumber,

    @Schema(description = "비밀번호", example = "a0240120305812krlakdsflsa;1235", requiredMode = NOT_REQUIRED)
    String password
) {

}
