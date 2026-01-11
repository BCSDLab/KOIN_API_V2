package in.koreatech.koin.admin.notification.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.notification.dto.AdminNotificationRequest;
import in.koreatech.koin.admin.notification.service.AdminNotificationService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/notification")
public class AdminNotificationController implements AdminNotificationApi {

    private final AdminNotificationService adminNotificationService;

    @PostMapping("/send")
    public ResponseEntity<Void> sendNotification(
        @Valid @RequestBody AdminNotificationRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminNotificationService.sendNotification(request, adminId);
        return ResponseEntity.ok().build();
    }
}
