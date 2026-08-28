package in.koreatech.koin.domain.teamrecruitment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.teamrecruitment.dto.TeamRecruitmentNotificationsResponse;
import in.koreatech.koin.domain.teamrecruitment.service.TeamRecruitmentNotificationService;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/team-recruitments/notifications")
public class TeamRecruitmentNotificationController implements TeamRecruitmentNotificationApi {

    private final TeamRecruitmentNotificationService notificationService;

    @GetMapping
    public ResponseEntity<TeamRecruitmentNotificationsResponse> getNotifications(
            @Auth Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(notificationService.getNotifications(userId, page, limit));
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @Auth Integer userId,
            @PathVariable Integer notificationId
    ) {
        notificationService.markAsRead(userId, notificationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllRead(@Auth Integer userId) {
        notificationService.markAllRead(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll(@Auth Integer userId) {
        notificationService.deleteAll(userId);
        return ResponseEntity.ok().build();
    }
}
