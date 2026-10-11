package in.koreatech.koin.unit.domain.team.recruitment.service;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.ACCEPTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.PENDING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.REJECTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentNotification;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentApplicationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentNotificationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentOutboxEventRepository;
import in.koreatech.koin.domain.team.recruitment.service.TeamRecruitmentClosureService;
import in.koreatech.koin.unit.fixture.UserFixture;
import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeamRecruitmentClosureServiceTest {

    @Mock
    private TeamRecruitmentApplicationRepository applicationRepository;
    @Mock
    private TeamRecruitmentChatRoomRepository chatRoomRepository;
    @Mock
    private TeamRecruitmentNotificationRepository notificationRepository;
    @Mock
    private TeamRecruitmentOutboxEventRepository outboxEventRepository;
    @InjectMocks
    private TeamRecruitmentClosureService service;

    @Test
    void 마감_알림은_탈퇴하지_않은_지원자에게만_발송한다() {
        TeamRecruitment recruitment = TeamRecruitment.builder().id(10).title("모집").build();
        TeamRecruitmentApplication withdrawnPending = TeamRecruitmentApplication.builder()
            .id(20).recruitment(recruitment).status(PENDING).build();
        TeamRecruitmentApplication withdrawnAccepted = TeamRecruitmentApplication.builder()
            .id(21).recruitment(recruitment).status(ACCEPTED).build();
        TeamRecruitmentApplication remaining = TeamRecruitmentApplication.builder()
            .id(22).recruitment(recruitment).status(ACCEPTED)
            .applicant(UserFixture.id_설정_코인_유저(2)).build();
        when(applicationRepository.findAllByRecruitmentIdAndStatusInWithLock(10, List.of(PENDING)))
            .thenReturn(List.of(withdrawnPending));
        when(applicationRepository.findAllByRecruitmentIdAndStatusInWithLock(10, List.of(ACCEPTED)))
            .thenReturn(List.of(withdrawnAccepted, remaining));

        service.onClosed(recruitment);

        assertThat(withdrawnPending.getStatus()).isEqualTo(REJECTED);
        assertThat(withdrawnAccepted.getStatus()).isEqualTo(ACCEPTED);
        assertThat(remaining.getStatus()).isEqualTo(ACCEPTED);
        ArgumentCaptor<TeamRecruitmentNotification> notification =
            ArgumentCaptor.forClass(TeamRecruitmentNotification.class);
        verify(notificationRepository).save(notification.capture());
        assertThat(notification.getValue().getRecipient()).isEqualTo(remaining.getApplicant());
        verify(outboxEventRepository).save(any());
    }
}
