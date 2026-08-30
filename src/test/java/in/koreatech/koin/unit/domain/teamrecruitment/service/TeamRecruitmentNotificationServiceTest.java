package in.koreatech.koin.unit.domain.teamrecruitment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentNotification;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentNotificationRepository;
import in.koreatech.koin.domain.teamrecruitment.dto.TeamRecruitmentNotificationsResponse;
import in.koreatech.koin.domain.teamrecruitment.service.TeamRecruitmentNotificationService;

@ExtendWith(MockitoExtension.class)
class TeamRecruitmentNotificationServiceTest {

    private static final Integer USER_ID = 1;
    private static final Integer NOTIFICATION_ID = 10;

    @Mock
    private TeamRecruitmentNotificationRepository notificationRepository;

    @InjectMocks
    private TeamRecruitmentNotificationService notificationService;

    @Test
    void 알림_목록_조회시_미읽음_수와_페이지_정보를_반환한다() {
        TeamRecruitmentNotification notification = mock(TeamRecruitmentNotification.class);
        when(notification.getId()).thenReturn(NOTIFICATION_ID);
        when(notification.getType()).thenReturn(mock(in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.class));
        when(notification.getTargetType()).thenReturn(mock(in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationTargetType.class));
        when(notification.getRecruitment()).thenReturn(mock(in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment.class));
        when(notification.getRecruitment().getId()).thenReturn(1);
        when(notification.isRead()).thenReturn(false);

        when(notificationRepository.countByRecipient_IdAndIsDeletedFalse(USER_ID)).thenReturn(1L);
        when(notificationRepository.findAllByRecipient_IdAndIsDeletedFalseOrderByIdDesc(eq(USER_ID), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(notification)));
        when(notificationRepository.countByRecipient_IdAndReadAtIsNullAndIsDeletedFalse(USER_ID))
                .thenReturn(1L);

        TeamRecruitmentNotificationsResponse response = notificationService.getNotifications(USER_ID, 1, 10);

        assertThat(response.unreadCount()).isEqualTo(1L);
        assertThat(response.totalCount()).isEqualTo(1L);
        assertThat(response.currentPage()).isEqualTo(1);
        assertThat(response.notifications()).hasSize(1);
    }

    @Test
    void 존재하지_않는_알림을_읽음_처리하면_404를_반환한다() {
        when(notificationRepository.findByIdAndRecipient_Id(NOTIFICATION_ID, USER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(USER_ID, NOTIFICATION_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ApiResponseCode.TEAM_RECRUITMENT_NOTIFICATION_NOT_FOUND));
    }

    @Test
    void 전체_읽음_처리시_삭제되지_않은_알림을_모두_읽음_처리한다() {
        TeamRecruitmentNotification n1 = mock(TeamRecruitmentNotification.class);
        TeamRecruitmentNotification n2 = mock(TeamRecruitmentNotification.class);

        when(notificationRepository.findAllByRecipient_IdAndIsDeletedFalse(USER_ID))
                .thenReturn(List.of(n1, n2));

        notificationService.markAllRead(USER_ID);

        verify(n1).markAsRead(any());
        verify(n2).markAsRead(any());
    }

    @Test
    void 전체_삭제시_삭제되지_않은_알림을_모두_삭제한다() {
        TeamRecruitmentNotification n1 = mock(TeamRecruitmentNotification.class);
        TeamRecruitmentNotification n2 = mock(TeamRecruitmentNotification.class);

        when(notificationRepository.findAllByRecipient_IdAndIsDeletedFalse(USER_ID))
                .thenReturn(List.of(n1, n2));

        notificationService.deleteAll(USER_ID);

        verify(n1).delete();
        verify(n2).delete();
    }
}
