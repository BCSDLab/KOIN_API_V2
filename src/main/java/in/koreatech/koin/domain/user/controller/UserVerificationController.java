package in.koreatech.koin.domain.user.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.user.dto.FindIdRequest;
import in.koreatech.koin.domain.user.dto.FindIdResponse;
import in.koreatech.koin.domain.user.dto.ResetPasswordRequest;
import in.koreatech.koin.domain.user.dto.SendVerificationCodeRequest;
import in.koreatech.koin.domain.user.dto.VerificationCountRequest;
import in.koreatech.koin.domain.user.dto.VerificationCountResponse;
import in.koreatech.koin.domain.user.dto.VerifyVerificationCodeRequest;
import in.koreatech.koin.domain.user.service.UserService;
import in.koreatech.koin.domain.user.service.UserVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserVerificationController implements UserVerificationApi {

    private final UserVerificationService userVerificationService;
    private final UserService userService;

    @PostMapping("/user/verification/send")
    public ResponseEntity<Void> sendVerificationCode(
        @Valid @RequestBody SendVerificationCodeRequest request
    ) {
        userVerificationService.sendCode(request.target());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/verification/verify")
    public ResponseEntity<Void> verifyVerificationCode(
        @Valid @RequestBody VerifyVerificationCodeRequest request
    ) {
        userVerificationService.verifyCode(request.target(), request.code());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/verification/count")
    public ResponseEntity<VerificationCountResponse> getVerificationCount(
        @Valid @ParameterObject VerificationCountRequest request
    ) {
        VerificationCountResponse response = userVerificationService.getVerificationCount(request.target());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/id/find")
    public ResponseEntity<FindIdResponse> findIdByVerification(
        @Valid @RequestBody FindIdRequest request
    ) {
        String userId = userService.findIdByVerification(request.target());
        return ResponseEntity.ok().body(FindIdResponse.from(userId));
    }

    @PostMapping("/user/password/reset")
    public ResponseEntity<Void> resetPassword(
        @Valid @RequestBody ResetPasswordRequest request
    ) {
        userService.resetPasswordByVerification(request.userId(), request.target(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}
