package in.koreatech.koin.domain.timetableV2.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record TimetableFramesResponse(
    @Schema(description = "학기별 시간표 프레임", example = """
        {
            "20241": [
                {
                    "id": 1,
                    "timetable_name": "시간표1",
                    "is_main": true
                },
                {
                    "id": 2,
                    "timetable_name": "시간표2",
                    "is_main": false
                }
            ]
        }
        """, requiredMode = Schema.RequiredMode.REQUIRED)
    Map<String, List<TimetableFrameResponse>> semesters
) {
    public static TimetableFramesResponse from(List<TimetableFrame> timetableFrames) {
        Map<String, List<TimetableFrameResponse>> groupedBySemester = timetableFrames.stream()
            .collect(Collectors.groupingBy(
                frame -> frame.getSemester().getSemester(),
                Collectors.mapping(TimetableFrameResponse::from, Collectors.toList())
            ));
        return new TimetableFramesResponse(groupedBySemester);
    }
}
