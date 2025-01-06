package in.koreatech.koin.domain.timetableV3.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record TimetableFrameUpdateRequestV3(
    @Schema(description = "시간표 이름", example = "시간표1", requiredMode = REQUIRED)
    @Size(max = 255, message = "시간표 이름의 최대 길이는 255자입니다.")
    @NotBlank(message = "시간표 이름을 입력해주세요.")
    String name,

    @Schema(description = "메인 시간표 여부", example = "false", requiredMode = REQUIRED)
    @NotNull(message = "시간표 메인 여부를 입력해주세요.")
    Boolean isMain
) {
}
