package in.koreatech.koin.domain.timetableV2.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record TimeTableLecturesDeleteRequest(
    @Schema(description = "timetableLecture id 리스트", example = "[1, 2, 3]", requiredMode = REQUIRED)
    List<Integer> timetablesLectureIds
) {
}
