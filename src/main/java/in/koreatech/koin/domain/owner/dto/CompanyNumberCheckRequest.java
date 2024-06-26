package in.koreatech.koin.domain.owner.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@JsonNaming(SnakeCaseStrategy.class)
public record CompanyNumberCheckRequest(
    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}", message = "사업자 등록 번호 형식이 올바르지 않습니다. ${validatedValue}")
    @Schema(description = "사업자 등록 번호", example = "012-34-56789", requiredMode = REQUIRED)
    @NotBlank(message = "사업자 등록 번호를 입력해주세요.")
    String companyNumber
) {

}
