package in.koreatech.koin.domain.timetableV3.controller;

import static in.koreatech.koin.domain.user.model.UserType.COUNCIL;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.timetableV3.dto.response.TakeAllTimetableLectureResponse;
import in.koreatech.koin.domain.timetableV3.dto.response.TimetableLectureResponseV3;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Timetable: V3-시간표", description = "시간표를 관리한다")
public interface TimetableLectureApiV3 {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "시간표 강의 정보 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/v3/timetables/lecture")
    ResponseEntity<TimetableLectureResponseV3> getTimetableLecture(
        @RequestParam(value = "timetable_frame_id") Integer timetableFrameId,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "수강한 강의 정보 전체 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/v3/timetables/main/lectures")
    ResponseEntity<TakeAllTimetableLectureResponse> getTakeAllTimetableLectures(
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "삭제한 시간표 강의 복구")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/v3/timetables/lecture/rollback")
    ResponseEntity<TimetableLectureResponseV3> rollbackTimetableLecture(
        @RequestParam(name = "timetable_lectures_id") List<Integer> timetableLecturesId,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "삭제한 시간표 프레임과 강의 복구",
        description = """
            1. 삭제된 시간표 프레임: 삭제된 시간표 프레임과 그에 속한 강의 정보를 복구합니다. \n
            2. 삭제되지 않은 시간표 프레임: 시간표 프레임에 속한 강의 정보를 복구합니다.
            """)
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/v3/timetables/frame/rollback")
    ResponseEntity<TimetableLectureResponseV3> rollbackTimetableFrame(
        @RequestParam(name = "timetable_frame_id") Integer timetableFrameId,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    );
}
