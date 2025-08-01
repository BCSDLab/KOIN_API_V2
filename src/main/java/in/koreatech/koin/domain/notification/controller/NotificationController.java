package in.koreatech.koin.domain.notification.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.domain.notification.model.NotificationDetailSubscribeType;
import in.koreatech.koin.domain.notification.model.NotificationSubscribeType;
import in.koreatech.koin.domain.notification.service.NotificationService;
import in.koreatech.koin.domain.notification.dto.NotificationPermitRequest;
import in.koreatech.koin.domain.notification.dto.NotificationStatusResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

    @GetMapping("/notification")
    public ResponseEntity<NotificationStatusResponse> checkNotificationStatus(
        @Auth(permit = {GENERAL, STUDENT, OWNER, COOP, COUNCIL}) Integer userId
    ) {
        return ResponseEntity.ok(notificationService.getNotificationInfo(userId));
    }

    @PostMapping("/notification")
    public ResponseEntity<Void> permitNotification(
        @Auth(permit = {GENERAL, STUDENT, OWNER, COOP, COUNCIL}) Integer userId,
        @Valid @RequestBody NotificationPermitRequest request
    ) {
        notificationService.permitNotification(userId, request.deviceToken());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/notification/subscribe")
    public ResponseEntity<Void> permitNotificationSubscribe(
        @Auth(permit = {GENERAL, STUDENT, OWNER, COOP, COUNCIL}) Integer userId,
        @RequestParam(value = "type") NotificationSubscribeType notificationSubscribeType
    ) {
        notificationService.permitNotificationSubscribe(userId, notificationSubscribeType);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/notification/subscribe/detail")
    public ResponseEntity<Void> permitNotificationDetailSubscribe(
        @Auth(permit = {GENERAL, STUDENT, OWNER, COOP, COUNCIL}) Integer userId,
        @RequestParam(value = "detail_type") NotificationDetailSubscribeType detailSubscribeType
    ) {
        notificationService.permitNotificationDetailSubscribe(userId, detailSubscribeType);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/notification")
    public ResponseEntity<Void> rejectNotification(
        @Auth(permit = {GENERAL, STUDENT, OWNER, COOP, COUNCIL}) Integer userId
    ) {
        notificationService.rejectNotification(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/notification/subscribe")
    public ResponseEntity<Void> rejectNotificationSubscribe(
        @Auth(permit = {GENERAL, STUDENT, OWNER, COOP, COUNCIL}) Integer userId,
        @RequestParam(value = "type") NotificationSubscribeType notificationSubscribeType
    ) {
        notificationService.rejectNotificationBySubscribeType(userId, notificationSubscribeType);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/notification/subscribe/detail")
    public ResponseEntity<Void> rejectNotificationDetailSubscribe(
        @Auth(permit = {GENERAL, STUDENT, OWNER, COOP, COUNCIL}) Integer userId,
        @RequestParam(value = "detail_type") NotificationDetailSubscribeType detailSubscribeType
    ) {
        notificationService.rejectNotificationByDetailType(userId, detailSubscribeType);
        return ResponseEntity.noContent().build();
    }
}
