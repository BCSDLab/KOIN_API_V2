package in.koreatech.koin.domain.user.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.domain.user.dto.validation.CheckEmailDuplicationRequest;
import in.koreatech.koin.domain.user.dto.validation.CheckLoginIdDuplicationRequest;
import in.koreatech.koin.domain.user.dto.validation.CheckNicknameDuplicationRequest;
import in.koreatech.koin.domain.user.dto.validation.CheckPhoneDuplicationRequest;
import in.koreatech.koin.domain.user.dto.validation.CheckUserPasswordRequest;
import in.koreatech.koin.domain.user.dto.validation.ExistsByEmailRequest;
import in.koreatech.koin.domain.user.dto.validation.ExistsByPhoneRequest;
import in.koreatech.koin.domain.user.dto.validation.ExistsByUserIdRequest;
import in.koreatech.koin.domain.user.dto.validation.MatchUserIdWithEmailRequest;
import in.koreatech.koin.domain.user.dto.validation.MatchUserIdWithPhoneNumberRequest;
import in.koreatech.koin.domain.user.service.UserValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserValidationController implements UserValidationApi {

    private final UserValidationService userValidationService;

    @GetMapping("/user/check/email")
    public ResponseEntity<Void> checkUserEmailExist(
        @ParameterObject @ModelAttribute(value = "address")
        @Valid CheckEmailDuplicationRequest request
    ) {
        userValidationService.checkDuplicatedEmail(request.email());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/check/phone")
    public ResponseEntity<Void> checkPhoneNumberExist(
        @ParameterObject @ModelAttribute(value = "phone")
        @Valid CheckPhoneDuplicationRequest request
    ) {
        userValidationService.checkDuplicatedPhoneNumber(request.phone());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/check/nickname")
    public ResponseEntity<Void> checkDuplicationOfNickname(
        @ParameterObject @ModelAttribute("nickname")
        @Valid CheckNicknameDuplicationRequest request
    ) {
        userValidationService.checkDuplicatedNickname(request.nickname());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/check/id")
    public ResponseEntity<Void> checkDuplicatedLoginId(
        @ParameterObject @ModelAttribute("id")
        @Valid CheckLoginIdDuplicationRequest request
    ) {
        userValidationService.checkDuplicatedLoginId(request.loginId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/check/password")
    public ResponseEntity<Void> checkPassword(
        @Valid @RequestBody CheckUserPasswordRequest request,
        @Auth(permit = {GENERAL, STUDENT, OWNER, COOP, COUNCIL}) Integer userId
    ) {
        userValidationService.checkPassword(request.password(), userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/id/exists")
    public ResponseEntity<Void> existsByUserId(
        @Valid @RequestBody ExistsByUserIdRequest request
    ) {
        userValidationService.existsByUserId(request.loginId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/phone/exists")
    public ResponseEntity<Void> existsByPhoneNumber(
        @Valid @RequestBody ExistsByPhoneRequest request
    ) {
        userValidationService.existsByPhoneNumber(request.phoneNumber());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/email/exists")
    public ResponseEntity<Void> existsByEmail(
        @Valid @RequestBody ExistsByEmailRequest request
    ) {
        userValidationService.existsByEmail(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/id/match/phone")
    public ResponseEntity<Void> matchUserIdWithPhoneNumber(
        @Valid @RequestBody MatchUserIdWithPhoneNumberRequest request
    ) {
        userValidationService.matchUserIdWithPhoneNumber(request.loginId(), request.phoneNumber());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/id/match/email")
    public ResponseEntity<Void> matchUserIdWithEmail(
        @Valid @RequestBody MatchUserIdWithEmailRequest request
    ) {
        userValidationService.matchUserIdWithEmail(request.loginId(), request.email());
        return ResponseEntity.ok().build();
    }
}
