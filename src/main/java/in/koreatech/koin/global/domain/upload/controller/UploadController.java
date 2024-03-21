package in.koreatech.koin.global.domain.upload.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.domain.upload.dto.UploadFileResponse;
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
        @Auth(permit = {OWNER, STUDENT}) Long memberId
    ) {
        var response = uploadService.getPresignedUrl(domain, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(
        value = "/{domain}/upload/file",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UploadFileResponse> uploadFile(
        @PathVariable ImageUploadDomain domain,
        @RequestPart MultipartFile multipartFile,
        @Auth(permit = {OWNER, STUDENT}) Long memberId
    ) {
        var response = uploadService.uploadFile(domain, multipartFile);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
