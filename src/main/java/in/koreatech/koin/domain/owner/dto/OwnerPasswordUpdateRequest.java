package in.koreatech.koin.domain.owner.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record OwnerPasswordUpdateRequest(
    @NotNull(message = "비밀번호는 비워둘 수 없습니다.")
    @Schema(description = "비밀번호", requiredMode = REQUIRED, example = "a0240120305812krlakdsflsa;1235")
    String password,

    @Email(message = "이메일 형식이 올바르지 않습니다. ${validatedValue}")
    @NotNull(message = "이메일은 필수입니다.")
    @Schema(description = "이메일 주소", requiredMode = REQUIRED, example = "asdf@gmail.com")
    @JsonProperty(value = "address")
    String email
) {

}
