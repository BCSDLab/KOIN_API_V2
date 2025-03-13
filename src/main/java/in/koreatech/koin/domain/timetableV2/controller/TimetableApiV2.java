package in.koreatech.koin.domain.timetableV2.controller;

import static in.koreatech.koin.domain.user.model.UserType.COUNCIL;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.timetableV2.dto.request.TimetableFrameCreateRequest;
import in.koreatech.koin.domain.timetableV2.dto.request.TimetableFrameUpdateRequest;
import in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureCreateRequest;
import in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureUpdateRequest;
import in.koreatech.koin.domain.timetableV2.dto.response.TimetableFrameResponse;
import in.koreatech.koin.domain.timetableV2.dto.response.TimetableLectureResponse;
import in.koreatech.koin._common.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) Timetable: V2-시간표", description = "시간표 정보를 관리한다")
public interface TimetableApiV2 {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "시간표 프레임 생성")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/v2/timetables/frame")
    ResponseEntity<TimetableFrameResponse> createTimetablesFrame(
        @Valid @RequestBody TimetableFrameCreateRequest request,
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
    @Operation(summary = "시간표 프레임 수정")
    @SecurityRequirement(name = "Jwt Authentication")
    @PutMapping("/v2/timetables/frame/{id}")
    ResponseEntity<TimetableFrameResponse> updateTimetableFrame(
        @Valid @RequestBody TimetableFrameUpdateRequest request,
        @PathVariable(value = "id") Integer timetableFrameId,
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
    @Operation(summary = "시간표 프레임 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/v2/timetables/frames")
    ResponseEntity<Object> getTimetablesFrame(
        @RequestParam(name = "semester", required = false) String semester,
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
    @Operation(summary = "시간표 프레임 삭제")
    @SecurityRequirement(name = "Jwt Authentication")
    @DeleteMapping("/v2/timetables/frame")
    ResponseEntity<Void> deleteTimetablesFrame(
        @RequestParam(name = "id") Integer frameId,
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
    @Operation(summary = "시간표 프레임 모두 삭제")
    @DeleteMapping("/v2/all/timetables/frame")
    ResponseEntity<Void> deleteAllTimetablesFrame(
        @RequestParam(name = "semester") String semester,
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
    @Operation(summary = "시간표에 강의 정보 추가",
        description = """
            lecture_id가 있는 경우 class_infos, professor은 null, grades는 '0'으로 입력해야합니다.\n
            lecture_id가 없는 경우 class_infos, professor, grades을 선택적으로 입력합니다.
            """)
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/v2/timetables/lecture")
    ResponseEntity<TimetableLectureResponse> createTimetableLecture(
        @RequestBody TimetableLectureCreateRequest request,
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
    @Operation(summary = "시간표에서 강의 정보 수정")
    @SecurityRequirement(name = "Jwt Authentication")
    @PutMapping("/v2/timetables/lecture")
    ResponseEntity<TimetableLectureResponse> updateTimetableLecture(
        @RequestBody TimetableLectureUpdateRequest request,
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
    @Operation(summary = "시간표에서 강의 정보 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/v2/timetables/lecture")
    ResponseEntity<TimetableLectureResponse> getTimetableLecture(
        @RequestParam(value = "timetable_frame_id") Integer timetableFrameId,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "시간표에서 강의 정보 삭제")
    @SecurityRequirement(name = "Jwt Authentication")
    @DeleteMapping("/v2/timetables/lecture/{id}")
    ResponseEntity<Void> deleteTimetableLecture(
        @PathVariable(value = "id") Integer timetableLectureId,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "시간표에서 여러 개의 강의 정보 삭제")
    @SecurityRequirement(name = "Jwt Authentication")
    @DeleteMapping("/v2/timetables/lectures")
    ResponseEntity<Void> deleteTimetableLectures(
        @RequestParam(name = "timetable_lecture_ids") List<Integer> request,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "시간표 프레임 Id를 이용해서 정규 강의 정보 삭제")
    @SecurityRequirement(name = "Jwt Authentication")
    @DeleteMapping("/v2/timetables/frame/{frameId}/lecture/{lectureId}")
    ResponseEntity<Void> deleteTimetableLectureByFrameId(
        @PathVariable(value = "frameId") Integer frameId,
        @PathVariable(value = "lectureId") Integer lectureId,
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
    @PostMapping("/v2/timetables/lecture/rollback")
    ResponseEntity<TimetableLectureResponse> rollbackTimetableLecture(
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
    @PostMapping("/v2/timetables/frame/rollback")
    ResponseEntity<TimetableLectureResponse> rollbackTimetableFrame(
        @RequestParam(name = "timetable_frame_id") Integer timetableFrameId,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    );
}
