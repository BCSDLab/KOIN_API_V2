package in.koreatech.koin.domain.graduation.controller;

import java.io.IOException;
import java.util.List;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.domain.graduation.dto.CourseTypeLectureResponse;
import in.koreatech.koin.domain.graduation.dto.GraduationCourseCalculationResponse;
import in.koreatech.koin.domain.graduation.model.GeneralEducationArea;
import in.koreatech.koin.domain.graduation.service.GraduationService;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class GraduationController implements GraduationApi {

    private final GraduationService graduationService;

    @PostMapping("/graduation/agree")
    public ResponseEntity<Void> createStudentCourseCalculation(
        @Auth(permit = {STUDENT}) Integer userId) {
        graduationService.createStudentCourseCalculation(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/graduation/excel/upload")
    public ResponseEntity<String> uploadStudentGradeExcelFile(
        @RequestParam(value = "file") MultipartFile file,
        @Auth(permit = {UserType.STUDENT}) Integer userId
    ) {
        try {
            graduationService.readStudentGradeExcelFile(file, userId);
            return ResponseEntity.ok("파일이 성공적으로 업로드되었습니다.");
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/graduation/course-type")
    public ResponseEntity<CourseTypeLectureResponse> getCourseTypeLecture(
        @RequestParam(name = "year") Integer year,
        @RequestParam(name = "term") String term,
        @RequestParam(name = "name") String courseTypeName,
        @RequestParam(name = "general_education_area", required = false) String generalEducationAreaName,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        CourseTypeLectureResponse response = graduationService.getLectureByCourseType(year, term,
            courseTypeName, generalEducationAreaName);
        return ResponseEntity.ok(response);
    }

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "교양영역 전체 조회")
    @GetMapping("/general-education-area")
    public ResponseEntity<List<GeneralEducationArea>> getCourseTypeLecture() {
        List<GeneralEducationArea> response = graduationService.getAllGeneralEducationArea();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/graduation/course/calculation")
    public ResponseEntity<GraduationCourseCalculationResponse> getGraduationCourseCalculation(
        @Auth(permit = {STUDENT}) Integer userId) {
        GraduationCourseCalculationResponse response = graduationService.getGraduationCourseCalculationResponse(userId);
        return ResponseEntity.ok(response);
    }
}
