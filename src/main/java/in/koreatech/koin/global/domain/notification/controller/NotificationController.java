package in.koreatech.koin.global.domain.notification.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.user.dto.NotificationPermitRequest;
import in.koreatech.koin.domain.user.dto.NotificationStatusResponse;
import in.koreatech.koin.domain.user.service.UserService;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.domain.notification.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

    private final UserService userService;
    private final NotificationService notificationService;

    @GetMapping("/notification")
    public ResponseEntity<NotificationStatusResponse> checkNotificationStatus(
        @Auth(permit = {STUDENT, OWNER, COOP}) Long memberId
    ) {
        return ResponseEntity.ok(notificationService.checkNotification(memberId));
    }

    @PostMapping("/notification")
    public ResponseEntity<Void> permitNotification(
        @Auth(permit = {STUDENT, OWNER, COOP}) Long memberId,
        @Valid @RequestBody NotificationPermitRequest request
    ) {
        userService.permitNotification(memberId, request.deviceToken());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/notification")
    public ResponseEntity<Void> rejectNotification(
        @Auth(permit = {STUDENT, OWNER, COOP}) Long memberId
    ) {
        userService.rejectNotification(memberId);
        return ResponseEntity.ok().build();
    }
}
