package in.koreatech.koin.unit.domain.team.recruitment.scheduler;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.ACCEPTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.PENDING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.REJECTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus.ACTIVE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus.READ_ONLY;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType.DIRECT;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType.TEAM;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.CLOSED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.RECRUITING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentNotification;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentOutboxEvent;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentApplicationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentNotificationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentOutboxEventRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.team.recruitment.scheduler.TeamRecruitmentDeadlineCloseProcessor;
import in.koreatech.koin.unit.fixture.UserFixture;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TeamRecruitmentDeadlineCloseProcessorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 28);

    @Mock
    private TeamRecruitmentRepository recruitmentRepository;

    @Mock
    private TeamRecruitmentApplicationRepository applicationRepository;

    @Mock
    private TeamRecruitmentChatRoomRepository chatRoomRepository;

    @Mock
    private TeamRecruitmentNotificationRepository notificationRepository;

    @Mock
    private TeamRecruitmentOutboxEventRepository outboxEventRepository;

    @InjectMocks
    private TeamRecruitmentDeadlineCloseProcessor processor;

    @Nested
    class CloseExpiredRecruitments {

        @Test
        void 기한이_지난_모집을_닫고_PENDING_지원과_모든_채팅방을_정리한다() {
            TeamRecruitment recruitment = recruitment(1, TODAY.minusDays(1));
            TeamRecruitmentApplication pending = application(11, recruitment, PENDING);
            TeamRecruitmentApplication accepted = application(12, recruitment, ACCEPTED);
            TeamRecruitmentChatRoom teamRoom = chatRoom(21, recruitment, TEAM, ACTIVE);
            TeamRecruitmentChatRoom directRoom = chatRoom(22, recruitment, DIRECT, ACTIVE);
            stubLockedRecruitment(recruitment);
            stubApplications(pending, accepted);
            when(chatRoomRepository.findByRecruitment_IdAndRoomScopeKey(1, "TEAM"))
                .thenReturn(Optional.of(teamRoom));
            when(chatRoomRepository.findAllByRecruitment_Id(1)).thenReturn(List.of(teamRoom, directRoom));
            when(outboxEventRepository.findByEventKey(any())).thenReturn(Optional.empty());
            when(notificationRepository.save(any())).thenAnswer(invocation -> {
                TeamRecruitmentNotification notification = invocation.getArgument(0);
                ReflectionTestUtils.setField(
                    notification,
                    "id",
                    notification.getApplication().getId() + 100
                );
                return notification;
            });

            processor.closeIfExpired(1, TODAY);

            assertThat(recruitment.getStatus()).isEqualTo(CLOSED);
            assertThat(pending.getStatus()).isEqualTo(REJECTED);
            assertThat(pending.getDecisionReason()).isEqualTo("RECRUITMENT_CLOSED");
            assertThat(accepted.getStatus()).isEqualTo(ACCEPTED);
            assertThat(teamRoom.getStatus()).isEqualTo(READ_ONLY);
            assertThat(directRoom.getStatus()).isEqualTo(READ_ONLY);
            verify(applicationRepository).save(pending);
            verify(chatRoomRepository).save(teamRoom);
            verify(chatRoomRepository).save(directRoom);
            verify(notificationRepository, org.mockito.Mockito.times(2)).save(any());
            verify(outboxEventRepository, org.mockito.Mockito.times(2)).save(any());
            ArgumentCaptor<TeamRecruitmentOutboxEvent> outboxCaptor =
                ArgumentCaptor.forClass(TeamRecruitmentOutboxEvent.class);
            verify(outboxEventRepository, org.mockito.Mockito.times(2)).save(outboxCaptor.capture());
            assertThat(outboxCaptor.getAllValues())
                .extracting(TeamRecruitmentOutboxEvent::getPayload)
                .anySatisfy(payload -> assertThat(payload).contains("\"notification_id\":111"))
                .anySatisfy(payload -> assertThat(payload).contains("\"notification_id\":112"));
        }

        @Test
        void 이미_마감된_모집은_다시_처리하지_않는다() {
            TeamRecruitment recruitment = recruitment(1, TODAY.minusDays(1));
            TeamRecruitmentApplication pending = application(11, recruitment, PENDING);
            stubLockedRecruitment(recruitment);

            processor.closeIfExpired(1, TODAY);
            clearInvocations(applicationRepository, chatRoomRepository, notificationRepository, outboxEventRepository);

            processor.closeIfExpired(1, TODAY);

            assertThat(recruitment.getStatus()).isEqualTo(CLOSED);
            assertThat(pending.getStatus()).isEqualTo(PENDING);
            verify(applicationRepository, never()).save(any());
            verify(chatRoomRepository, never()).save(any());
            verify(notificationRepository, never()).save(any());
            verify(outboxEventRepository, never()).save(any());
        }

        @Test
        void 승인된_지원자가_있는데_TEAM_채팅방이_없으면_내부_무결성_예외를_던진다() {
            TeamRecruitment recruitment = recruitment(1, TODAY.minusDays(1));
            TeamRecruitmentApplication accepted = application(12, recruitment, ACCEPTED);
            stubLockedRecruitment(recruitment);
            when(applicationRepository.findAllByRecruitment_IdAndStatusIn(
                1,
                List.of(PENDING),
                Pageable.unpaged()
            )).thenReturn(new PageImpl<>(List.of()));
            when(applicationRepository.findAllByRecruitment_IdAndStatusIn(
                1,
                List.of(ACCEPTED),
                Pageable.unpaged()
            )).thenReturn(new PageImpl<>(List.of(accepted)));
            when(chatRoomRepository.findByRecruitment_IdAndRoomScopeKey(1, "TEAM"))
                .thenReturn(Optional.empty());
            when(chatRoomRepository.findAllByRecruitment_Id(1)).thenReturn(List.of());

            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> processor.closeIfExpired(1, TODAY)
            );

            assertThat(exception).hasMessageContaining("TEAM 채팅방");
            verify(notificationRepository, never()).save(any());
            verify(outboxEventRepository, never()).save(any());
        }

        @Test
        void 승인된_지원자가_없으면_TEAM_채팅방이_없어도_정상_마감한다() {
            TeamRecruitment recruitment = recruitment(1, TODAY.minusDays(1));
            stubLockedRecruitment(recruitment);
            when(applicationRepository.findAllByRecruitment_IdAndStatusIn(
                1,
                List.of(PENDING),
                Pageable.unpaged()
            )).thenReturn(new PageImpl<>(List.of()));
            when(applicationRepository.findAllByRecruitment_IdAndStatusIn(
                1,
                List.of(ACCEPTED),
                Pageable.unpaged()
            )).thenReturn(new PageImpl<>(List.of()));
            when(chatRoomRepository.findByRecruitment_IdAndRoomScopeKey(1, "TEAM"))
                .thenReturn(Optional.empty());
            when(chatRoomRepository.findAllByRecruitment_Id(1)).thenReturn(List.of());

            processor.closeIfExpired(1, TODAY);

            assertThat(recruitment.getStatus()).isEqualTo(CLOSED);
            verify(notificationRepository, never()).save(any());
            verify(outboxEventRepository, never()).save(any());
        }
    }

    @Nested
    class SkipNonExpiredRecruitments {

        @Test
        void 오늘이_마감일이면_모집을_닫지_않는다() {
            TeamRecruitment recruitment = recruitment(1, TODAY);
            stubLockedRecruitment(recruitment);

            processor.closeIfExpired(1, TODAY);

            assertThat(recruitment.getStatus()).isEqualTo(RECRUITING);
            verify(applicationRepository, never()).findAllByRecruitment_IdAndStatusIn(any(), anyList(), any(Pageable.class));
            verify(chatRoomRepository, never()).findAllByRecruitment_Id(any());
        }
    }

    private void stubLockedRecruitment(TeamRecruitment recruitment) {
        when(recruitmentRepository.findByIdWithLock(recruitment.getId()))
            .thenReturn(Optional.of(recruitment));
    }

    private void stubApplications(
        TeamRecruitmentApplication pending,
        TeamRecruitmentApplication accepted
    ) {
        when(applicationRepository.findAllByRecruitment_IdAndStatusIn(
            1,
            List.of(PENDING),
            Pageable.unpaged()
        )).thenReturn(new PageImpl<>(List.of(pending)));
        when(applicationRepository.findAllByRecruitment_IdAndStatusIn(
            1,
            List.of(ACCEPTED),
            Pageable.unpaged()
        )).thenReturn(new PageImpl<>(List.of(accepted)));
    }

    private TeamRecruitment recruitment(Integer id, LocalDate deadlineDate) {
        return TeamRecruitment.builder()
            .id(id)
            .author(UserFixture.id_설정_코인_유저(1))
            .activityStartDate(TODAY.plusDays(1))
            .activityEndDate(TODAY.plusDays(10))
            .deadlineDate(deadlineDate)
            .maxParticipants(5)
            .currentParticipants(1)
            .status(RECRUITING)
            .description("모집 내용")
            .build();
    }

    private TeamRecruitmentApplication application(
        Integer id,
        TeamRecruitment recruitment,
        TeamRecruitmentApplicationStatus status
    ) {
        return TeamRecruitmentApplication.builder()
            .id(id)
            .recruitment(recruitment)
            .applicant(UserFixture.id_설정_코인_유저(id))
            .motivation("지원 동기")
            .availability("가능")
            .status(status)
            .profileSnapshot("{}")
            .build();
    }

    private TeamRecruitmentChatRoom chatRoom(
        Integer id,
        TeamRecruitment recruitment,
        in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType roomType,
        in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus status
    ) {
        return TeamRecruitmentChatRoom.builder()
            .id(id)
            .recruitment(recruitment)
            .roomScopeKey(roomType == TEAM ? "TEAM" : "APPLICATION:12")
            .roomType(roomType)
            .status(status)
            .build();
    }
}
