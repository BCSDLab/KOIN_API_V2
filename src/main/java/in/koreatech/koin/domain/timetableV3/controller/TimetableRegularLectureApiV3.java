package in.koreatech.koin.domain.timetableV3.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.domain.timetableV3.dto.request.TimetableRegularLectureCreateRequest;
import in.koreatech.koin.domain.timetableV3.dto.request.TimetableRegularLectureUpdateRequest;
import in.koreatech.koin.domain.timetableV3.dto.response.TimetableLectureResponseV3;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) Timetable: V3-시간표", description = "시간표 정규 강의 정보를 관리한다")
public interface TimetableRegularLectureApiV3 {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "정규 강의 생성")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/v3/timetables/lecture/regular")
    ResponseEntity<TimetableLectureResponseV3> createTimetablesRegularLecture(
        @Valid @RequestBody TimetableRegularLectureCreateRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "정규 강의 수정")
    @SecurityRequirement(name = "Jwt Authentication")
    @PutMapping("/v3/timetables/lecture/regular")
    ResponseEntity<TimetableLectureResponseV3> updateTimetablesRegularLecture(
        @Valid @RequestBody TimetableRegularLectureUpdateRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    );
}
