package in.koreatech.koin.domain.timetable.controller;

import static in.koreatech.koin.domain.user.model.UserType.COUNCIL;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.timetable.dto.LectureResponse;
import in.koreatech.koin.domain.timetable.dto.SemesterCheckResponse;
import in.koreatech.koin.domain.timetable.dto.SemesterResponse;
import in.koreatech.koin.domain.timetable.dto.TimetableCreateRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableResponse;
import in.koreatech.koin.domain.timetable.dto.TimetableUpdateRequest;
import in.koreatech.koin._common.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Lecture: 시간표", description = "시간표 정보를 관리한다")
public interface TimetableApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "강의 목록 조회")
    @GetMapping("/lectures")
    ResponseEntity<List<LectureResponse>> getLecture(
        String semesterDate
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "학기 정보 조회")
    @GetMapping("/semesters")
    ResponseEntity<List<SemesterResponse>> getSemesters();

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "학생 학기 정보 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/semesters/check")
    ResponseEntity<SemesterCheckResponse> getStudentSemesters(
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
    @Operation(summary = "시간표 정보 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/timetables")
    ResponseEntity<TimetableResponse> getTimetables(
        @RequestParam(value = "semester") String semester,
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
    @Operation(summary = "시간표 정보 생성")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/timetables")
    ResponseEntity<TimetableResponse> createTimetables(
        @RequestBody TimetableCreateRequest request,
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
    @Operation(summary = "시간표 정보 수정")
    @SecurityRequirement(name = "Jwt Authentication")
    @PutMapping("/timetables")
    ResponseEntity<TimetableResponse> updateTimetable(
        @RequestBody TimetableUpdateRequest request,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "204", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "시간표 삭제")
    @SecurityRequirement(name = "Jwt Authentication")
    @DeleteMapping("/timetable")
    ResponseEntity<Void> deleteTimetableById(
        @RequestParam(value = "id") Integer id,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    );
}
