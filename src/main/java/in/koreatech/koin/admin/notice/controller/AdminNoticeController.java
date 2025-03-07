package in.koreatech.koin.admin.notice.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.notice.dto.AdminNoticeRequest;
import in.koreatech.koin.admin.notice.dto.AdminNoticeResponse;
import in.koreatech.koin.admin.notice.dto.AdminNoticesResponse;
import in.koreatech.koin.admin.notice.service.AdminNoticeService;
import in.koreatech.koin._common.auth.Auth;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/notice")
public class AdminNoticeController implements AdminNoticeApi {

    private final AdminNoticeService adminNoticeService;

    @GetMapping
    public ResponseEntity<AdminNoticesResponse> getAllNotices(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @RequestParam(name = "is_deleted", defaultValue = "false") Boolean isDeleted,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().body(adminNoticeService.getNotices(page, limit, isDeleted));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminNoticeResponse> getNotice(
        @Parameter(in = PATH) @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().body(adminNoticeService.getNotice(id));
    }

    @PostMapping
    public ResponseEntity<Void> createNotice(
        @RequestBody @Valid AdminNoticeRequest adminNoticeRequest,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminNoticeService.createNotice(adminNoticeRequest, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(
        @PathVariable("id") Integer noticeId,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminNoticeService.deleteNotice(noticeId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateNotice(
        @PathVariable("id") Integer noticeId,
        @RequestBody @Valid AdminNoticeRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminNoticeService.updateNotice(noticeId, request);
        return ResponseEntity.ok().build();
    }

}
