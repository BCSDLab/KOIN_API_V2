package in.koreatech.koin.domain.teamrecruitment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.teamrecruitment.dto.TeamRecruitmentNotificationsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "팀원 모집 알림", description = "팀원 모집 알림 API")
public interface TeamRecruitmentNotificationApi {

    @Operation(summary = "팀원 모집 알림 목록 조회")
    ResponseEntity<TeamRecruitmentNotificationsResponse> getNotifications(
            Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit
    );

    @Operation(summary = "알림 읽음 처리")
    ResponseEntity<Void> markAsRead(
            Integer userId,
            @PathVariable Integer notificationId
    );

    @Operation(summary = "모든 알림 읽음 처리")
    ResponseEntity<Void> markAllRead(Integer userId);

    @Operation(summary = "모든 알림 삭제")
    ResponseEntity<Void> deleteAll(Integer userId);
}
