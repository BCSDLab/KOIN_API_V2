package in.koreatech.koin.domain.club.recruitment.controller;

import in.koreatech.koin.domain.club.recruitment.dto.request.ClubRecruitmentCreateRequest;
import in.koreatech.koin.domain.club.recruitment.dto.request.ClubRecruitmentModifyRequest;
import in.koreatech.koin.domain.club.recruitment.dto.response.ClubRecruitmentResponse;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static in.koreatech.koin.global.code.ApiResponseCode.*;

@Tag(name = "(Normal) Club Recruitment: 동아리 모집 공고", description = "동아리 모집 공고를 관리한다")
@RequestMapping("/clubs/{clubId}/recruitment")
public interface ClubRecruitmentApi {

    @ApiResponseCodes({
        OK,
        INVALID_RECRUITMENT_PERIOD,
        MUST_BE_NULL_RECRUITMENT_PERIOD,
        REQUIRED_RECRUITMENT_PERIOD,
        NOT_FOUND_CLUB,
        NOT_FOUND_USER,
        DUPLICATE_CLUB_RECRUITMENT,
        INVALID_REQUEST_BODY,
    })
    @Operation(summary = "동아리 모집 생성", description = """
        ### 동아리 모집 생성
        - 동아리 모집 생성을 합니다.
        - 모집 시작 기간과 모집 마감 기간은 "yyyy-MM-dd" 형식입니다.
        - 상시 모집 여부의 기본값은 false입니다.
        - 상시 모집 여부가 true인 경우, 모집 시작 기간과 모집 마감 기간은 null로 요청하셔야 합니다.
        """)
    @PostMapping
    ResponseEntity<Void> createRecruitment(
        @RequestBody @Valid ClubRecruitmentCreateRequest request,
        @Parameter(description = "동아리 고유 식별자(clubId)", example = "1") @PathVariable Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponseCodes({
        OK,
        INVALID_RECRUITMENT_PERIOD,
        MUST_BE_NULL_RECRUITMENT_PERIOD,
        REQUIRED_RECRUITMENT_PERIOD,
        NOT_FOUND_CLUB,
        NOT_FOUND_USER,
        NOT_FOUND_CLUB_RECRUITMENT,
        INVALID_REQUEST_BODY
    })
    @Operation(summary = "동아리 모집 수정", description = """
        ### 동아리 모집 수정
        - 동아리 모집을 수정 합니다.
        - 모집 시작 기간과 모집 마감 기간은 "yyyy-MM-dd" 형식입니다.
        - 상시 모집 여부가 true인 경우, 모집 시작 기간과 모집 마감 기간은 null로 요청하셔야 합니다.
        """)
    @PutMapping
    ResponseEntity<Void> modifyRecruitment(
        @RequestBody @Valid ClubRecruitmentModifyRequest request,
        @Parameter(description = "동아리 고유 식별자(clubId)", example = "1") @PathVariable Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponseCodes({
        NO_CONTENT,
        NOT_FOUND_CLUB,
        NOT_FOUND_USER,
        NOT_FOUND_CLUB_RECRUITMENT,
        INVALID_REQUEST_BODY
    })
    @Operation(summary = "동아리 모집 삭제", description = """
        ### 동아리 모집 수정
        - 동아리 모집을 삭제 합니다.
        """)
    @DeleteMapping
    ResponseEntity<Void> deleteRecruitment(
        @Parameter(description = "동아리 고유 식별자(clubId)", example = "1") @PathVariable Integer clubId,
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponseCodes({
        OK,
        NOT_FOUND_CLUB,
        NOT_FOUND_CLUB_RECRUITMENT,
        INVALID_REQUEST_BODY
    })
    @Operation(summary = "동아리 모집 조회", description = """
        ### 동아리 모집 조회
        - 동아리 모집을 조회 합니다.
            - status : 동아리 모집 상태입니다.
              - NONE : 동아리 모집 글이 없는 상태입니다.
              - BEFORE : 동아리 모집 글이 있으며 모집 전 상태입니다.
              - RECRUITING : 동아리 모집 중인 상태입니다.
              - CLOSED : 동아리 모집 글이 있는 상태에서 모집 마감된 상태입니다.
              - ALWAYS : 동아리 모집 글이 있는 상태에서 상시 모집중인 상태입니다.
            - Dday : 동아리 모집 디데이 입니다.
              - RECRUITING인 상태에서는 정수로 남은 일자가 내려갑니다.
              - 이외의 상태에서는 null로 내려갑니다.
        """)
    @GetMapping
    ResponseEntity<ClubRecruitmentResponse> getRecruitment(
        @Parameter(description = "동아리 고유 식별자(clubId)", example = "1") @PathVariable(name = "clubId") Integer clubId,
        @UserId Integer userId
    );
}
