package in.koreatech.koin.domain.owner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OwnerSendEmailRequest(
    @Email(message = "이메일 형식이 올바르지 않습니다. ${validatedValue}")
    @NotBlank(message = "이메일은 필수입니다.")
    @JsonProperty(value = "address")
    String email
) {

}
