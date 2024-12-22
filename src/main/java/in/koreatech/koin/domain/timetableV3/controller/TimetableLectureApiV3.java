package in.koreatech.koin.domain.timetableV3.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.timetableV2.dto.response.TimetableLectureResponse;
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
        @Auth(permit = {STUDENT}) Integer userId
    );
}
