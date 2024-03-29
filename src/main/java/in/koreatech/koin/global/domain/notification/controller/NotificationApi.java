package in.koreatech.koin.global.domain.notification.controller;

import static in.koreatech.koin.domain.user.model.UserType.COOP;
import static in.koreatech.koin.domain.user.model.UserType.OWNER;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.domain.notification.NotificationSubscribeType;
import in.koreatech.koin.global.domain.notification.dto.NotificationPermitRequest;
import in.koreatech.koin.global.domain.notification.dto.NotificationStatusResponse;
import in.koreatech.koin.global.domain.notification.dto.NotificationSubscribePermitRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) Notification: 알림", description = "알림 관련 API")
public interface NotificationApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "푸쉬알림 동의 여부 조회")
    @GetMapping("/notification")
    ResponseEntity<NotificationStatusResponse> checkNotificationStatus(
        @Auth(permit = {STUDENT, OWNER, COOP}) Long userId,
        @Valid @ModelAttribute("type") NotificationSubscribeType notificationSubscribeType
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400"),
            @ApiResponse(responseCode = "401"),
            @ApiResponse(responseCode = "403"),
            @ApiResponse(responseCode = "404"),
        }
    )
    @Operation(summary = "푸쉬알림 동의")
    @PostMapping("/notification")
    ResponseEntity<Void> permitNotification(
        @Auth(permit = {STUDENT, OWNER, COOP}) Long userId,
        @Valid @RequestBody NotificationPermitRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400"),
            @ApiResponse(responseCode = "401"),
            @ApiResponse(responseCode = "403"),
            @ApiResponse(responseCode = "404"),
        }
    )
    @Operation(summary = "특정 푸쉬알림 구독")
    @PostMapping("/notification/subscribe")
    ResponseEntity<Void> permitNotificationSubscribe(
        @Auth(permit = {STUDENT, OWNER, COOP}) Long userId,
        @Valid @RequestBody NotificationSubscribePermitRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "400"),
            @ApiResponse(responseCode = "401"),
            @ApiResponse(responseCode = "403"),
            @ApiResponse(responseCode = "404"),
        }
    )
    @Operation(summary = "푸쉬알림 거절")
    @DeleteMapping("/notification")
    ResponseEntity<Void> rejectNotification(
        @Auth(permit = {STUDENT, OWNER, COOP}) Long userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "400"),
            @ApiResponse(responseCode = "401"),
            @ApiResponse(responseCode = "403"),
            @ApiResponse(responseCode = "404"),
        }
    )
    @Operation(summary = "특정 푸쉬알림 구독 취소")
    @DeleteMapping("/notification/subscribe")
    ResponseEntity<Void> rejectNotificationSubscribe(
        @Auth(permit = {STUDENT, OWNER, COOP}) Long userId,
        @Valid @ModelAttribute("type") NotificationSubscribeType notificationSubscribeType
    );
}
