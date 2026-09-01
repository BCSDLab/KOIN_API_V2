package in.koreatech.koin.domain.team.recruitment.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static in.koreatech.koin.global.code.ApiResponseCode.FORBIDDEN_USER_TYPE;
import static in.koreatech.koin.global.code.ApiResponseCode.UNAUTHORIZED_USER;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_START_DATE_AFTER_END_DATE;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_USER;
import static in.koreatech.koin.global.code.ApiResponseCode.OK;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_ACTIVITY_END_DATE_MUST_BE_NULL;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_ACTIVITY_END_DATE_REQUIRED;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_PROFILE_NOT_FOUND;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.domain.team.recruitment.dto.TeamRecruitmentProfileResponse;
import in.koreatech.koin.domain.team.recruitment.dto.TeamRecruitmentProfileUpsertRequest;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) Team Recruitment Profile: 팀원 모집 프로필", description = "팀원 모집 전용 프로필을 관리한다")
@RequestMapping("/team-recruitment-profiles")
public interface TeamRecruitmentProfileApi {

    @ApiResponseCodes({
        OK,
        NOT_FOUND_USER,
        TEAM_RECRUITMENT_PROFILE_NOT_FOUND,
        UNAUTHORIZED_USER,
        FORBIDDEN_USER_TYPE,
    })
    @Operation(summary = "내 팀원 모집 프로필 조회", description = """
        ### 내 팀원 모집 프로필 조회
        - 현재 인증된 사용자의 팀원 모집 전용 프로필을 조회합니다.
        - 학과/학부, 전공, 학번은 학적 정보에서 가져옵니다.
        - 전공 구분이 없는 학부는 전공이 null입니다.
        - 저장된 프로필이 없으면 404를 반환합니다.
        """)
    @GetMapping("/me")
    ResponseEntity<TeamRecruitmentProfileResponse> getMyProfile(
        @Auth(permit = {STUDENT}) Integer studentId
    );

    @ApiResponseCodes({
        OK,
        NOT_FOUND_USER,
        TEAM_RECRUITMENT_ACTIVITY_END_DATE_REQUIRED,
        TEAM_RECRUITMENT_ACTIVITY_END_DATE_MUST_BE_NULL,
        INVALID_START_DATE_AFTER_END_DATE,
        INVALID_REQUEST_BODY,
        UNAUTHORIZED_USER,
        FORBIDDEN_USER_TYPE,
    })
    @Operation(summary = "팀원 모집 프로필 생성 또는 수정", description = """
        ### 팀원 모집 프로필 생성 또는 수정
        - 현재 인증된 사용자의 프로필을 저장합니다. 프로필이 없으면 생성하고, 있으면 수정합니다.
        - 보유 기술과 활동 내역은 기존 목록을 전부 대체하며, 요청한 순서대로 저장됩니다.
        - 활동 내역의 각 항목에 id를 보내지 않습니다.
        - 활동 시작일과 종료일은 "yyyy-MM-dd" 형식입니다.
        - 진행 중인 활동인 경우, 활동 종료일은 null로 요청하셔야 합니다.
        - 진행 중인 활동이 아닌 경우, 활동 종료일은 필수이며 활동 시작일과 같거나 이후여야 합니다.
        - 학과/학부, 전공, 학번 변경은 학적 정보 수정 API를 사용하셔야 합니다.
        """)
    @PutMapping("/me")
    ResponseEntity<TeamRecruitmentProfileResponse> upsertMyProfile(
        @RequestBody @Valid TeamRecruitmentProfileUpsertRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    );
}
