package in.koreatech.koin.domain.team.recruitment.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.team.recruitment.dto.TeamRecruitmentProfileResponse;
import in.koreatech.koin.domain.team.recruitment.dto.TeamRecruitmentProfileUpsertRequest;
import in.koreatech.koin.domain.team.recruitment.service.TeamRecruitmentProfileService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/team-recruitment-profiles")
public class TeamRecruitmentProfileController implements TeamRecruitmentProfileApi {

    private final TeamRecruitmentProfileService teamRecruitmentProfileService;

    @GetMapping("/me")
    public ResponseEntity<TeamRecruitmentProfileResponse> getMyProfile(
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        TeamRecruitmentProfileResponse response = teamRecruitmentProfileService.getMyProfile(studentId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<TeamRecruitmentProfileResponse> upsertMyProfile(
        @RequestBody @Valid TeamRecruitmentProfileUpsertRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        TeamRecruitmentProfileResponse response =
            teamRecruitmentProfileService.upsertMyProfile(studentId, request);
        return ResponseEntity.ok(response);
    }
}
