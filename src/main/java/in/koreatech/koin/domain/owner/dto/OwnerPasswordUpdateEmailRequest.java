package in.koreatech.koin.domain.owner.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OwnerPasswordUpdateEmailRequest(
    @JsonProperty(value = "address")
    @NotBlank(message = "검증값은 필수입니다.")
    @Schema(description = "검증값 (전화번호, 이메일)", example = "01012341234", requiredMode = REQUIRED)
    String address,

    @NotNull(message = "비밀번호는 비워둘 수 없습니다.")
    @Schema(description = "비밀번호", example = "a0240120305812krlakdsflsa;1235", requiredMode = REQUIRED)
    String password
) {

}
