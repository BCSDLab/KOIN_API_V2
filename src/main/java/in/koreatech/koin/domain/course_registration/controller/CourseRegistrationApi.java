package in.koreatech.koin.domain.course_registration.controller;

import static in.koreatech.koin.domain.user.model.UserType.COUNCIL;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.course_registration.dto.CourseRegistrationLectureResponse;
import in.koreatech.koin.domain.course_registration.dto.PreCourseRegistrationLectureResponse;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Course Registration: 수강 신청 연습", description = "수강 신청 연습 정보를 관리한다")
public interface CourseRegistrationApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "예비 수강 신청 과목 조회", description = """
        ### 예비 수강 신청 과목 조회 (로그인 필요)
        - **timetable_frame_id**: 시간표 프레임 ID
        - **department**: 학과 (커스텀 수업인 경우 "-")
        - **class_number**: 분반 (커스텀 수업인 경우 null)
        - **lecture_info.lecture_code**: 과목 코드 (커스텀 수업인 경우 null)
        - **grades**: 학점 (커스텀 수업인 경우 0)
        - **class_time_raw**: 수업 교시 원본 데이터 (int 배열)
        """)
    @GetMapping("/course/registration/precourse")
    ResponseEntity<List<PreCourseRegistrationLectureResponse>> getPreRegistrationLecture(
        @RequestParam(value = "timetable_frame_id") Integer timetableFrameId,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "전체 수강 신청 가능 과목 조회", description = """
        ### 수강 신청 가능 과목 조회
        - **name**: 과목 이름
        - **department**: 학과
            - HRD학과
            - 전기ㆍ전자ㆍ통신공학부
            - 산업경영학부
            - 교양학부
            - 기계공학부
            - 컴퓨터공학부
            - 메카트로닉스공학부
            - 에너지신소재화학공학부
            - 디자인ㆍ건축공학부
            - 미래융합학부
            - 고용서비스정책학과
        - **year**: 학년도 ex) 2024, 2025 ...
        - **semester**: 학기
            - **1학기**
            - **여름학기**
            - **2학기**
            - **겨울학기**
        """)
    @GetMapping("/course/registration/search")
    ResponseEntity<List<CourseRegistrationLectureResponse>> searchLectures(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String department,
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) String semester
    );
}
