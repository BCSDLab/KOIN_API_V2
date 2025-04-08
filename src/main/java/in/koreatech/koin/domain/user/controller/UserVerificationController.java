package in.koreatech.koin.domain.user.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.user.dto.SendVerificationCodeRequest;
import in.koreatech.koin.domain.user.dto.VerificationCountRequest;
import in.koreatech.koin.domain.user.dto.VerificationCountResponse;
import in.koreatech.koin.domain.user.dto.VerifyVerificationCodeRequest;
import in.koreatech.koin.domain.user.service.UserVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/verification")
public class UserVerificationController implements UserVerificationApi {

    private final UserVerificationService userVerificationService;

    @PostMapping("/send")
    public ResponseEntity<Void> sendVerificationCode(
        @Valid @RequestBody SendVerificationCodeRequest request
    ) {
        userVerificationService.sendCode(request.target());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verifyVerificationCode(
        @Valid @RequestBody VerifyVerificationCodeRequest request
    ) {
        userVerificationService.verifyCode(request.target(), request.code());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/count")
    public ResponseEntity<VerificationCountResponse> getVerificationCount(
        @Valid @ParameterObject VerificationCountRequest request
    ) {
        VerificationCountResponse response = userVerificationService.getVerificationCount(request.target());
        return ResponseEntity.ok(response);
    }
}
