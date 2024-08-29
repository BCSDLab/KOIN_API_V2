package in.koreatech.koin.global.domain.notification.controller;

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
import in.koreatech.koin.global.domain.notification.dto.NotificationPermitRequest;
import in.koreatech.koin.global.domain.notification.dto.NotificationStatusResponse;
import in.koreatech.koin.global.domain.notification.model.NotificationDetailSubscribeType;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType;
import in.koreatech.koin.global.domain.notification.service.NotificationService;
import in.koreatech.koin.global.ipaddress.IpAddress;
import in.koreatech.koin.global.useragent.UserAgent;
import in.koreatech.koin.global.useragent.UserAgentInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

    @GetMapping("/notification")
    public ResponseEntity<NotificationStatusResponse> checkNotificationStatus(
        @Auth(permit = {STUDENT, OWNER, COOP}) Integer userId,
        @IpAddress String ipAddress
    ) {
        return ResponseEntity.ok(notificationService.checkNotification(userId, ipAddress));
    }

    @PostMapping("/notification")
    public ResponseEntity<Void> permitNotification(
        @Auth(permit = {STUDENT, OWNER, COOP}) Integer userId,
        @UserAgent UserAgentInfo userAgentInfo,
        @IpAddress String ipAddress,
        @Valid @RequestBody NotificationPermitRequest request
    ) {
        notificationService.permitNotification(userId, userAgentInfo, ipAddress, request.deviceToken());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/notification/subscribe")
    public ResponseEntity<Void> permitNotificationSubscribe(
        @Auth(permit = {STUDENT, OWNER, COOP}) Integer userId,
        @IpAddress String ipAddress,
        @RequestParam(value = "type") NotificationSubscribeType notificationSubscribeType
    ) {
        notificationService.permitNotificationSubscribe(userId, ipAddress, notificationSubscribeType);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/notification/subscribe/detail")
    public ResponseEntity<Void> permitNotificationDetailSubscribe(
        @Auth(permit = {STUDENT, OWNER, COOP}) Integer userId,
        @IpAddress String ipAddress,
        @RequestParam(value = "detail_type") NotificationDetailSubscribeType detailSubscribeType
    ) {
        notificationService.permitNotificationDetailSubscribe(userId, ipAddress, detailSubscribeType);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/notification")
    public ResponseEntity<Void> rejectNotification(
        @Auth(permit = {STUDENT, OWNER, COOP}) Integer userId,
        @IpAddress String ipAddress
    ) {
        notificationService.rejectNotification(userId, ipAddress);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/notification/subscribe")
    public ResponseEntity<Void> rejectNotificationSubscribe(
        @Auth(permit = {STUDENT, OWNER, COOP}) Integer userId,
        @IpAddress String ipAddress,
        @RequestParam(value = "type") NotificationSubscribeType notificationSubscribeType
    ) {
        notificationService.rejectNotificationByType(userId, ipAddress, notificationSubscribeType);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/notification/subscribe/detail")
    public ResponseEntity<Void> rejectNotificationDetailSubscribe(
        @Auth(permit = {STUDENT, OWNER, COOP}) Integer userId,
        @IpAddress String ipAddress,
        @RequestParam(value = "detail_type") NotificationDetailSubscribeType detailSubscribeType
    ) {
        notificationService.rejectNotificationDetailSubscribe(userId, ipAddress, detailSubscribeType);
        return ResponseEntity.noContent().build();
    }
}
