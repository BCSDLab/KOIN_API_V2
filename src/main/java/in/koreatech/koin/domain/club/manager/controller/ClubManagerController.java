package in.koreatech.koin.domain.club.manager.controller;

import in.koreatech.koin.domain.club.manager.dto.request.ClubManagerEmpowermentRequest;
import in.koreatech.koin.domain.club.manager.service.ClubManagerService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clubs")
public class ClubManagerController implements ClubManagerApi {

    private final ClubManagerService clubManagerService;

    @PutMapping("/empowerment")
    public ResponseEntity<Void> empowermentClubManager(
        @RequestBody @Valid ClubManagerEmpowermentRequest request,
        @Auth(permit = {STUDENT}) Integer studentId
    ) {
        clubManagerService.empowermentClubManager(request, studentId);
        return ResponseEntity.ok().build();
    }
}
