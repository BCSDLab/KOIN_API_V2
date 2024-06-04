package in.koreatech.koin.domain.timetable.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TimetableFrameUpdateRequest(
    @Schema(description = "학기 정보", example = "20192")
    @NotBlank(message = "학기 정보를 입력해주세요.")
    String semester,

    @Schema(description = "메인 시간표 여부", example = "false", requiredMode = REQUIRED)
    @Size(max = 255, message = "시간표 이름의 최대 길이는 255자입니다.")
    @NotBlank(message = "시간표 이름을 입력해주세요.")
    String name,

    @Schema(description = "메인 시간표 여부", example = "false", requiredMode = REQUIRED)
    @NotBlank(message = "시간표 메인 여부를 입력해주세요.")
    boolean isMain
) {

}
