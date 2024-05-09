package in.koreatech.koin.global.domain.notification.controller;

import static in.koreatech.koin.domain.user.model.UserType.COOP;
import static in.koreatech.koin.domain.user.model.UserType.OWNER;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.domain.notification.dto.NotificationPermitRequest;
import in.koreatech.koin.global.domain.notification.dto.NotificationStatusResponse;
import in.koreatech.koin.global.domain.notification.model.NotificationDetailSubscribeType;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType;
import in.koreatech.koin.global.domain.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

    @GetMapping("/notification")
    public ResponseEntity<NotificationStatusResponse> checkNotificationStatus(
        @Auth(permit = {STUDENT, OWNER, COOP}) Integer userId
    ) {
        return ResponseEntity.ok(notificationService.checkNotification(userId));
    }

    @PostMapping("/notification")
    public ResponseEntity<Void> permitNotification(
        @Auth(permit = {STUDENT, OWNER, COOP}) Integer userId,
        @Valid @RequestBody NotificationPermitRequest request
    ) {
        notificationService.permitNotification(userId, request.deviceToken());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/notification/subscribe")
    public ResponseEntity<Void> permitNotificationSubscribe(
        @Auth(permit = {STUDENT, OWNER, COOP}) Integer userId,
        @RequestParam(value = "type") NotificationSubscribeType notificationSubscribeType
    ) {
        notificationService.permitNotificationSubscribe(userId, notificationSubscribeType);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/notification/subscribe/detail")
    public ResponseEntity<Void> permitNotificationDetailSubscribe(
        @Auth(permit = {STUDENT, OWNER, COOP}) Integer userId,
        @RequestParam(value = "detail_type") NotificationDetailSubscribeType detailSubscribeType
    ) {
        notificationService.permitNotificationDetailSubscribe(userId, detailSubscribeType);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/notification")
    public ResponseEntity<Void> rejectNotification(
        @Auth(permit = {STUDENT, OWNER, COOP}) Integer userId
    ) {
        notificationService.rejectNotification(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/notification/subscribe")
    public ResponseEntity<Void> rejectNotificationSubscribe(
        @Auth(permit = {STUDENT, OWNER, COOP}) Integer userId,
        @RequestParam(value = "type") NotificationSubscribeType notificationSubscribeType
    ) {
        notificationService.rejectNotificationByType(userId, notificationSubscribeType);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/notification/subscribe/detail")
    public ResponseEntity<Void> rejectNotificationDetailSubscribe(
        @Auth(permit = {STUDENT, OWNER, COOP}) Integer userId,
        @RequestParam(value = "detail_type") NotificationDetailSubscribeType detailSubscribeType
    ) {
        notificationService.rejectNotificationDetailSubscribe(userId, detailSubscribeType);
        return ResponseEntity.noContent().build();
    }
}
