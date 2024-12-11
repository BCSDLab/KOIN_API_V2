package in.koreatech.koin.domain.owner.dto.email;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OwnerPasswordUpdateEmailRequest(
    @Email(message = "이메일 형식이 올바르지 않습니다. ${validatedValue}")
    @NotBlank(message = "이메일은 필수입니다.")
    @JsonProperty(value = "address")
    @Schema(description = "이메일", example = "temp@gmail.com", requiredMode = REQUIRED)
    String address,

    @NotBlank(message = "비밀번호는 비워둘 수 없습니다.")
    @Schema(description = "비밀번호", example = "a0240120305812krlakdsflsa;1235", requiredMode = REQUIRED)
    String password
) {

}
