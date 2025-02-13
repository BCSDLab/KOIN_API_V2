package in.koreatech.koin.domain.timetableV3.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.timetableV3.dto.response.LectureResponseV3;
import in.koreatech.koin.global.auth.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Lecture: V3-정규 강의", description = "정규 강의 정보를 관리한다")
public interface LectureApiV3 {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "정규 강의 목록 조회")
    @GetMapping("/v3/lectures")
    ResponseEntity<List<LectureResponseV3>> getLectures(
        @RequestParam(name = "year") Integer year,
        @RequestParam(name = "term") String term
    );
}
