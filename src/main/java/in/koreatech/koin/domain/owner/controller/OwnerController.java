package in.koreatech.koin.domain.owner.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.owner.dto.VerifyEmailRequest;
import in.koreatech.koin.domain.owner.service.OwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    @PostMapping("/owners/register/verification/email")
    public ResponseEntity<Void> requestVerificationToRegister(@RequestBody @Valid VerifyEmailRequest request) {
        ownerService.requestVerificationToRegister(request);
        return ResponseEntity.ok().build();
    }
}
