package in.koreatech.koin.domain.graduation.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.domain.graduation.dto.CourseTypeLectureResponse;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Graduation: 졸업학점 계산기", description = "졸업학점 계산기 정보를 관리한다")
public interface GraduationApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "졸업학점 계산 동의")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/graduation/agree")
    ResponseEntity<Void> createStudentCourseCalculation(
        @Auth(permit = {STUDENT}) Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "엑셀 성적 정보 업로드")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/graduation/excel/upload")
    ResponseEntity<String> uploadStudentGradeExcelFile(
        @RequestParam(value = "file") MultipartFile file,
        @Auth(permit = {STUDENT}) Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "학기에 따른 이수구분 강의 출력")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/graduation/course-type")
    ResponseEntity<CourseTypeLectureResponse> getCourseTypeLecture(
        @RequestParam(name = "year") Integer year,
        @RequestParam(name = "term") String term,
        @RequestParam(name = "name") String courseTypeName,
        @Auth(permit = {STUDENT}) Integer userId
    );
}
