package in.koreatech.koin.admin.lecture.controller;

import static in.koreatech.koin.admin.history.enums.DomainType.LECTURES;
import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static in.koreatech.koin.global.code.ApiResponseCode.DUPLICATE_LECTURE;
import static in.koreatech.koin.global.code.ApiResponseCode.FORBIDDEN_ADMIN;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_SEMESTER;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_READABLE_HTTP_MESSAGE;
import static in.koreatech.koin.global.code.ApiResponseCode.OK;
import static in.koreatech.koin.global.code.ApiResponseCode.UNAUTHORIZED_USER;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.admin.history.aop.AdminActivityLogging;
import in.koreatech.koin.admin.lecture.dto.AdminLectureCreateRequest;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Admin) Lecture: 강의", description = "관리자 권한으로 강의를 관리한다")
public interface AdminLectureApi {

    @ApiResponseCodes({
        OK,
        INVALID_REQUEST_BODY,
        UNAUTHORIZED_USER,
        FORBIDDEN_ADMIN,
        NOT_READABLE_HTTP_MESSAGE,
        NOT_FOUND_SEMESTER,
        DUPLICATE_LECTURE
    })
    @Operation(
        summary = "강의를 일괄 생성한다",
        description = """
            ## 강의 일괄 생성
            입력한 연도와 학기에 해당하는 강의들을 한 번에 생성합니다.
            요청한 학기가 존재하지 않거나 중복 강의가 포함된 경우 강의를 생성하지 않습니다.
            중복 여부는 학기, 과목 코드, 분반의 조합을 기준으로 판단합니다.

            ## 요청 Body 필드 설명
            - `year`: 강의를 등록할 연도 (필수, 양수)
            - `term`: 강의를 등록할 학기 (필수)
              - `FIRST`: 1학기
              - `SECOND`: 2학기
              - `SUMMER`: 여름학기
              - `WINTER`: 겨울학기
            - `lectures`: 생성할 강의 정보 리스트 (필수, 빈 리스트 불가)
              - `code`: 과목 코드 (필수, 최대 10자)
              - `name`: 과목 이름 (필수, 최대 50자)
              - `grades`: 대상 학년 (필수, 최대 2자)
              - `lecture_class`: 분반 (필수, 최대 3자)
              - `regular_number`: 수강 인원 (필수, 최대 4자)
              - `department`: 학부 (필수, 최대 30자)
              - `target`: 수강 대상 (필수, 최대 200자)
              - `professor`: 교수명 (선택, 최대 30자)
              - `is_english`: 영어 강의 여부 (필수, 최대 2자)
              - `design_score`: 설계 학점 (필수, 최대 2자)
              - `is_elearning`: 이러닝 여부 (필수, 최대 2자)
              - `class_time`: 강의 시간 코드 리스트 (필수, 최대 50개, 각 값은 0~999)

            ## 처리 결과
            - 모든 강의가 유효한 경우 일괄 생성하고 `200 OK`를 반환합니다.
            - 요청 리스트 내부 또는 기존 강의와 중복되는 항목이 있으면 `DUPLICATE_LECTURE`를 반환합니다.
            """
    )
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/admin/lectures")
    @AdminActivityLogging(domain = LECTURES)
    ResponseEntity<Void> createLectures(
        @RequestBody @Valid AdminLectureCreateRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
