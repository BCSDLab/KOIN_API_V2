package in.koreatech.koin.global.domain.upload.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.user.model.UserType;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.domain.upload.dto.UploadUrlRequest;
import in.koreatech.koin.global.domain.upload.dto.UploadUrlResponse;
import in.koreatech.koin.global.domain.upload.model.ImageUploadDomain;
import in.koreatech.koin.global.domain.upload.service.UploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UploadController implements UploadApi {

    private final UploadService uploadService;

    @PostMapping("/{domain}/upload/url")
    public ResponseEntity<UploadUrlResponse> getPresignedUrl(
        @PathVariable ImageUploadDomain domain,
        @RequestBody @Valid UploadUrlRequest request,
        @Auth(permit = {UserType.OWNER, STUDENT}) Long memberId
    ) {
        var response = uploadService.getPresignedUrl(domain, request);
        return ResponseEntity.ok(response);
    }
}
