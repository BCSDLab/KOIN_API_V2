package in.koreatech.koin.admin.notification.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_DETAIL_SUBSCRIBE_TYPE;
import static in.koreatech.koin.global.code.ApiResponseCode.OK;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.admin.notification.dto.AdminNotificationRequest;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Admin) Notification: 알림", description = "알림 관련 API")
@RequestMapping("/admin/notification")
public interface AdminNotificationApi {

    @ApiResponseCodes(value = {
        OK,
        INVALID_DETAIL_SUBSCRIBE_TYPE
    })
    @Operation(description = "푸시 알림 전송")
    @PostMapping("/send")
    ResponseEntity<Void> sendNotification(
        @Valid @RequestBody AdminNotificationRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
