package in.koreatech.koin.domain.user.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.domain.user.dto.validation.UserAccessTokenRequest;
import in.koreatech.koin.domain.user.dto.validation.UserCorrectPasswordRequest;
import in.koreatech.koin.domain.user.dto.validation.UserExistsEmailRequest;
import in.koreatech.koin.domain.user.dto.validation.UserExistsLoginIdRequest;
import in.koreatech.koin.domain.user.dto.validation.UserExistsPhoneNumberRequest;
import in.koreatech.koin.domain.user.dto.validation.UserMatchLoginIdWithEmailRequest;
import in.koreatech.koin.domain.user.dto.validation.UserMatchLoginIdWithPhoneNumberRequest;
import in.koreatech.koin.domain.user.dto.validation.UserUniqueEmailRequest;
import in.koreatech.koin.domain.user.dto.validation.UserUniqueLoginIdRequest;
import in.koreatech.koin.domain.user.dto.validation.UserUniqueNicknameRequest;
import in.koreatech.koin.domain.user.dto.validation.UserUniquePhoneNumberRequest;
import in.koreatech.koin.domain.user.service.UserValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserValidationController implements UserValidationApi {

    private final UserValidationService userValidationService;

    @GetMapping("/user/check/login")
    public ResponseEntity<Void> requireLogin(
        @ParameterObject @ModelAttribute(value = "access_token")
        @Valid UserAccessTokenRequest request
    ) {
        userValidationService.requireLogin(request.accessToken());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/check/id")
    public ResponseEntity<Void> requireUniqueLoginId(
        @ParameterObject @ModelAttribute("id")
        @Valid UserUniqueLoginIdRequest request
    ) {
        userValidationService.requireUniqueLoginId(request.loginId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/check/phone")
    public ResponseEntity<Void> requireUniquePhoneNumber(
        @ParameterObject @ModelAttribute(value = "phone")
        @Valid UserUniquePhoneNumberRequest request
    ) {
        userValidationService.requireUniquePhoneNumber(request.phone());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/check/email")
    public ResponseEntity<Void> requireUniqueEmail(
        @ParameterObject @ModelAttribute(value = "address")
        @Valid UserUniqueEmailRequest request
    ) {
        userValidationService.requireUniqueEmail(request.email());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/check/nickname")
    public ResponseEntity<Void> requireUniqueNickname(
        @ParameterObject @ModelAttribute("nickname")
        @Valid UserUniqueNicknameRequest request
    ) {
        userValidationService.requireUniqueNickname(request.nickname());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/check/password")
    public ResponseEntity<Void> requireCorrectPassword(
        @Valid @RequestBody UserCorrectPasswordRequest request,
        @Auth(permit = {GENERAL, STUDENT, OWNER, COOP, COUNCIL}) Integer userId
    ) {
        userValidationService.requireCorrectPassword(request.password(), userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/id/exists")
    public ResponseEntity<Void> requireLoginIdExists(
        @Valid @RequestBody UserExistsLoginIdRequest request
    ) {
        userValidationService.requireLoginIdExists(request.loginId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/phone/exists")
    public ResponseEntity<Void> requirePhoneNumberExists(
        @Valid @RequestBody UserExistsPhoneNumberRequest request
    ) {
        userValidationService.requirePhoneNumberExists(request.phoneNumber());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/email/exists")
    public ResponseEntity<Void> requireEmailExists(
        @Valid @RequestBody UserExistsEmailRequest request
    ) {
        userValidationService.requireEmailExists(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/id/match/phone")
    public ResponseEntity<Void> matchLoginIdWithPhoneNumber(
        @Valid @RequestBody UserMatchLoginIdWithPhoneNumberRequest request
    ) {
        userValidationService.matchLoginIdWithPhoneNumber(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/id/match/email")
    public ResponseEntity<Void> matchLoginIdWithEmail(
        @Valid @RequestBody UserMatchLoginIdWithEmailRequest request
    ) {
        userValidationService.matchLoginIdWithEmail(request);
        return ResponseEntity.ok().build();
    }
}
