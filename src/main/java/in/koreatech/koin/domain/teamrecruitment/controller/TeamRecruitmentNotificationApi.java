package in.koreatech.koin.domain.teamrecruitment.controller;

import static in.koreatech.koin.global.code.ApiResponseCode.FORBIDDEN_USER_TYPE;
import static in.koreatech.koin.global.code.ApiResponseCode.OK;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_NOTIFICATION_NOT_FOUND;
import static in.koreatech.koin.global.code.ApiResponseCode.UNAUTHORIZED_USER;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.teamrecruitment.dto.TeamRecruitmentNotificationsResponse;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Team Recruitment Notification: 팀원 모집 알림", description = "팀원 모집 알림 API")
public interface TeamRecruitmentNotificationApi {

    @ApiResponseCodes({
        OK,
        UNAUTHORIZED_USER,
        FORBIDDEN_USER_TYPE,
    })
    @Operation(summary = "팀원 모집 알림 목록 조회")
    ResponseEntity<TeamRecruitmentNotificationsResponse> getNotifications(
            Integer userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    );

    @ApiResponseCodes({
        OK,
        TEAM_RECRUITMENT_NOTIFICATION_NOT_FOUND,
        UNAUTHORIZED_USER,
        FORBIDDEN_USER_TYPE,
    })
    @Operation(summary = "알림 읽음 처리")
    ResponseEntity<Void> markAsRead(
            Integer userId,
            @PathVariable Integer notificationId
    );

    @ApiResponseCodes({
        OK,
        UNAUTHORIZED_USER,
        FORBIDDEN_USER_TYPE,
    })
    @Operation(summary = "모든 알림 읽음 처리")
    ResponseEntity<Void> markAllRead(Integer userId);

    @ApiResponseCodes({
        OK,
        UNAUTHORIZED_USER,
        FORBIDDEN_USER_TYPE,
    })
    @Operation(summary = "모든 알림 삭제")
    ResponseEntity<Void> deleteAll(Integer userId);
}
