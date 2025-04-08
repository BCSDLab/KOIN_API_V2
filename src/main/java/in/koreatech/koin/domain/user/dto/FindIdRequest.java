package in.koreatech.koin.domain.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import in.koreatech.koin._common.validation.EmailOrPhone;
import io.swagger.v3.oas.annotations.media.Schema;

public record FindIdRequest(
    @EmailOrPhone
    @Schema(description = "전화번호 또는 코리아텍 이메일", example = "01000000000 or test@koreatech.ac.kr", requiredMode = REQUIRED)
    String target
) {

}
