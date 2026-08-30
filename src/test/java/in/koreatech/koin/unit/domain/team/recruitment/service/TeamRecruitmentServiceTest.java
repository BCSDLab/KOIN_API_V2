package in.koreatech.koin.unit.domain.team.recruitment.service;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.REJECTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.CLOSED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.DELETED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType.GENERAL;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType.ROLE_BASED;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_CLOSED;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_FORBIDDEN;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_MAX_PARTICIPANTS_BELOW_ACCEPTED;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_NOT_FOUND;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_ROLE_NOT_FOUND;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_ROLE_UPDATE_NOT_ALLOWED;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_TYPE_CHANGE_NOT_ALLOWED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.domain.team.recruitment.dto.UpdateRecruitmentRequest;
import in.koreatech.koin.domain.team.recruitment.dto.UpdateRoleInput;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentRole;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentApplicationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMemberRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.team.recruitment.service.TeamRecruitmentClosureService;
import in.koreatech.koin.domain.team.recruitment.service.TeamRecruitmentService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.unit.fixture.UserFixture;
import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
class TeamRecruitmentServiceTest {

    private static final Integer AUTHOR_ID = 1;
    private static final Integer OTHER_USER_ID = 2;
    private static final Integer RECRUITMENT_ID = 17;
    private static final Integer ROLE_ID = 4;
    private static final LocalDate DEADLINE = LocalDate.of(2026, 9, 3);
    private static final LocalDate ACTIVITY_START = LocalDate.of(2026, 9, 7);
    private static final LocalDate ACTIVITY_END = LocalDate.of(2026, 9, 30);

    @InjectMocks
    private TeamRecruitmentService teamRecruitmentService;

    @Mock
    private TeamRecruitmentRepository teamRecruitmentRepository;

    @Mock
    private TeamRecruitmentApplicationRepository applicationRepository;

    @Mock
    private TeamRecruitmentChatRoomRepository chatRoomRepository;

    @Mock
    private TeamRecruitmentChatMemberRepository chatMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamRecruitmentClosureService closureService;

    @Mock
    private EntityManager entityManager;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-08-30T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    private TeamRecruitment recruitment;

    @BeforeEach
    void setUp() {
        User author = UserFixture.id_설정_코인_유저(AUTHOR_ID);
        recruitment = TeamRecruitment.builder()
            .author(author)
            .category(TeamRecruitmentCategory.CONTEST)
            .title("AI 아이디어 공모전 팀원 모집")
            .meetingType(TeamRecruitmentMeetingType.ONLINE)
            .activityStartDate(ACTIVITY_START)
            .activityEndDate(ACTIVITY_END)
            .deadlineDate(DEADLINE)
            .recruitmentType(ROLE_BASED)
            .maxParticipants(3)
            .currentParticipants(0)
            .description("공모전 팀원을 모집합니다.")
            .build();
        ReflectionTestUtils.setField(recruitment, "id", RECRUITMENT_ID);
        recruitment.addRole(role(ROLE_ID, "PM", 3, 1));
    }

    private static TeamRecruitmentRole role(Integer id, String name, int maxParticipants, int displayOrder) {
        TeamRecruitmentRole role = TeamRecruitmentRole.builder()
            .name(name)
            .maxParticipants(maxParticipants)
            .currentParticipants(0)
            .displayOrder(displayOrder)
            .build();
        ReflectionTestUtils.setField(role, "id", id);
        return role;
    }

    private static UpdateRecruitmentRequest request(
        TeamRecruitmentType recruitmentType,
        Integer maxParticipants,
        List<UpdateRoleInput> roles
    ) {
        return new UpdateRecruitmentRequest(
            TeamRecruitmentCategory.CONTEST,
            "수정된 제목",
            TeamRecruitmentMeetingType.MIXED,
            ACTIVITY_START,
            ACTIVITY_END,
            DEADLINE,
            recruitmentType,
            maxParticipants,
            roles,
            "수정했습니다.",
            null,
            null
        );
    }

    private void givenFoundRecruitment() {
        when(teamRecruitmentRepository.findByIdWithLock(RECRUITMENT_ID)).thenReturn(Optional.of(recruitment));
    }

    private void givenNoApplicants() {
        lenient().when(applicationRepository.countByRecruitment_IdAndStatusIn(anyInt(), any())).thenReturn(0L);
        lenient().when(applicationRepository.countByRole_IdAndStatus(anyInt(), any())).thenReturn(0L);
    }

    @Nested
    @DisplayName("소유권 검증")
    class Ownership {

        @Test
        @DisplayName("작성자가 아니면 수정할 수 없다")
        void otherUserCannotUpdate() {
            givenFoundRecruitment();

            assertThatThrownBy(() -> teamRecruitmentService.updateRecruitment(
                OTHER_USER_ID, RECRUITMENT_ID, request(ROLE_BASED, null, List.of(
                    new UpdateRoleInput(ROLE_ID, "PM", 3)))))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TEAM_RECRUITMENT_FORBIDDEN);
        }

        @Test
        @DisplayName("작성자가 아니면 삭제할 수 없다")
        void otherUserCannotDelete() {
            givenFoundRecruitment();

            assertThatThrownBy(() -> teamRecruitmentService.deleteRecruitment(OTHER_USER_ID, RECRUITMENT_ID))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TEAM_RECRUITMENT_FORBIDDEN);
        }

        @Test
        @DisplayName("작성자가 아니면 마감할 수 없다")
        void otherUserCannotClose() {
            givenFoundRecruitment();

            assertThatThrownBy(() -> teamRecruitmentService.closeRecruitment(OTHER_USER_ID, RECRUITMENT_ID))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TEAM_RECRUITMENT_FORBIDDEN);
        }

        @Test
        @DisplayName("존재하지 않는 모집글은 404이다")
        void notFoundRecruitment() {
            when(teamRecruitmentRepository.findByIdWithLock(RECRUITMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> teamRecruitmentService.closeRecruitment(AUTHOR_ID, RECRUITMENT_ID))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TEAM_RECRUITMENT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("마감과 삭제")
    class CloseAndDelete {

        @Test
        @DisplayName("마감하면 상태가 CLOSED 가 된다")
        void closeChangesStatus() {
            givenFoundRecruitment();

            teamRecruitmentService.closeRecruitment(AUTHOR_ID, RECRUITMENT_ID);

            assertThat(recruitment.getStatus()).isEqualTo(CLOSED);
            verify(closureService).onClosed(recruitment);
        }

        @Test
        @DisplayName("이미 마감된 모집글에 다시 요청해도 예외가 발생하지 않는다")
        void closeIsIdempotent() {
            ReflectionTestUtils.setField(recruitment, "status", CLOSED);
            givenFoundRecruitment();

            assertThatCode(() -> teamRecruitmentService.closeRecruitment(AUTHOR_ID, RECRUITMENT_ID))
                .doesNotThrowAnyException();
            verify(closureService, never()).onClosed(any());
        }

        @Test
        @DisplayName("삭제하면 상태가 DELETED 이고 삭제 시각이 기록된다")
        void deleteMarksDeleted() {
            givenFoundRecruitment();

            teamRecruitmentService.deleteRecruitment(AUTHOR_ID, RECRUITMENT_ID);

            assertThat(recruitment.getStatus()).isEqualTo(DELETED);
            assertThat(recruitment.getDeletedAt()).isNotNull();
            verify(closureService).onDeleted(recruitment);
        }

        @Test
        @DisplayName("이미 삭제된 모집글에 다시 요청해도 예외가 발생하지 않는다")
        void deleteIsIdempotent() {
            recruitment.markDeleted(LocalDateTime.of(2026, 8, 30, 0, 0));
            givenFoundRecruitment();

            assertThatCode(() -> teamRecruitmentService.deleteRecruitment(AUTHOR_ID, RECRUITMENT_ID))
                .doesNotThrowAnyException();
            verify(closureService, never()).onDeleted(any());
        }

        @Test
        @DisplayName("마감된 모집글은 수정할 수 없다")
        void closedRecruitmentCannotBeUpdated() {
            ReflectionTestUtils.setField(recruitment, "status", CLOSED);
            givenFoundRecruitment();

            assertThatThrownBy(() -> teamRecruitmentService.updateRecruitment(
                AUTHOR_ID, RECRUITMENT_ID, request(ROLE_BASED, null, List.of(
                    new UpdateRoleInput(ROLE_ID, "PM", 3)))))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TEAM_RECRUITMENT_CLOSED);
        }
    }

    @Nested
    @DisplayName("정원과 모집 유형 수정 제약")
    class CapacityAndType {

        @Test
        @DisplayName("승인된 인원보다 적은 정원으로 수정하면 예외가 발생한다")
        void cannotShrinkBelowAcceptedCount() {
            ReflectionTestUtils.setField(recruitment, "currentParticipants", 3);
            givenFoundRecruitment();
            givenNoApplicants();

            assertThatThrownBy(() -> teamRecruitmentService.updateRecruitment(
                AUTHOR_ID, RECRUITMENT_ID, request(GENERAL, 1, List.of())))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TEAM_RECRUITMENT_MAX_PARTICIPANTS_BELOW_ACCEPTED);
        }

        @Test
        @DisplayName("지원자가 있으면 모집 유형을 변경할 수 없다")
        void cannotChangeTypeWithApplicants() {
            givenFoundRecruitment();
            when(applicationRepository.countByRecruitment_IdAndStatusIn(anyInt(), any())).thenReturn(1L);

            assertThatThrownBy(() -> teamRecruitmentService.updateRecruitment(
                AUTHOR_ID, RECRUITMENT_ID, request(GENERAL, 5, List.of())))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TEAM_RECRUITMENT_TYPE_CHANGE_NOT_ALLOWED);
        }

        @Test
        @DisplayName("거절된 지원서만 있어도 모집 유형을 변경할 수 없다")
        void cannotChangeTypeWithRejectedApplication() {
            givenFoundRecruitment();
            when(applicationRepository.countByRecruitment_IdAndStatusIn(anyInt(), any())).thenReturn(1L);

            assertThatThrownBy(() -> teamRecruitmentService.updateRecruitment(
                AUTHOR_ID, RECRUITMENT_ID, request(GENERAL, 5, List.of())))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TEAM_RECRUITMENT_TYPE_CHANGE_NOT_ALLOWED);
        }

        @Test
        @DisplayName("모집 유형 검증은 거절을 포함한 모든 상태를 센다")
        void typeChangeCountsEveryStatus() {
            givenFoundRecruitment();
            givenNoApplicants();

            teamRecruitmentService.updateRecruitment(AUTHOR_ID, RECRUITMENT_ID, request(GENERAL, 5, List.of()));

            verify(applicationRepository).countByRecruitment_IdAndStatusIn(
                RECRUITMENT_ID, List.of(TeamRecruitmentApplicationStatus.values()));
        }

        @Test
        @DisplayName("지원자가 없으면 모집 유형을 변경할 수 있다")
        void canChangeTypeWithoutApplicants() {
            givenFoundRecruitment();
            givenNoApplicants();

            teamRecruitmentService.updateRecruitment(AUTHOR_ID, RECRUITMENT_ID, request(GENERAL, 5, List.of()));

            assertThat(recruitment.getRecruitmentType()).isEqualTo(GENERAL);
            assertThat(recruitment.getMaxParticipants()).isEqualTo(5);
            assertThat(recruitment.getRoles()).isEmpty();
        }
    }

    @Nested
    @DisplayName("정원 충족 자동 마감")
    class CapacityAutoClose {

        @Test
        @DisplayName("정원을 승인 인원과 같게 줄이면 그 자리에서 마감된다")
        void closesWhenCapacityBecomesFull() {
            ReflectionTestUtils.setField(recruitment, "currentParticipants", 1);
            givenFoundRecruitment();
            givenNoApplicants();

            teamRecruitmentService.updateRecruitment(AUTHOR_ID, RECRUITMENT_ID, request(GENERAL, 1, List.of()));

            assertThat(recruitment.getStatus()).isEqualTo(CLOSED);
            verify(closureService).onCapacityFull(recruitment);
            verify(closureService, never()).onClosed(any());
        }

        @Test
        @DisplayName("정원이 남아 있으면 마감되지 않는다")
        void staysRecruitingWhenCapacityRemains() {
            ReflectionTestUtils.setField(recruitment, "currentParticipants", 1);
            givenFoundRecruitment();
            givenNoApplicants();

            teamRecruitmentService.updateRecruitment(AUTHOR_ID, RECRUITMENT_ID, request(GENERAL, 5, List.of()));

            assertThat(recruitment.isRecruiting()).isTrue();
            verify(closureService, never()).onCapacityFull(any());
        }
    }

    @Nested
    @DisplayName("역할 수정 제약")
    class RoleUpdate {

        @Test
        @DisplayName("해당 모집글의 역할이 아니면 404이다")
        void unknownRoleId() {
            givenFoundRecruitment();
            givenNoApplicants();

            assertThatThrownBy(() -> teamRecruitmentService.updateRecruitment(
                AUTHOR_ID, RECRUITMENT_ID, request(ROLE_BASED, null, List.of(
                    new UpdateRoleInput(999, "PM", 3)))))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TEAM_RECRUITMENT_ROLE_NOT_FOUND);
        }

        @Test
        @DisplayName("지원자가 있는 역할은 이름을 바꿀 수 없다")
        void cannotRenameRoleWithApplicants() {
            givenFoundRecruitment();
            when(applicationRepository.countByRole_IdAndStatus(anyInt(), any())).thenReturn(1L);

            assertThatThrownBy(() -> teamRecruitmentService.updateRecruitment(
                AUTHOR_ID, RECRUITMENT_ID, request(ROLE_BASED, null, List.of(
                    new UpdateRoleInput(ROLE_ID, "이름바꿈", 3)))))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TEAM_RECRUITMENT_ROLE_UPDATE_NOT_ALLOWED);
        }

        @Test
        @DisplayName("지원자가 있는 역할은 정원을 줄일 수 없다")
        void cannotShrinkRoleWithApplicants() {
            givenFoundRecruitment();
            when(applicationRepository.countByRole_IdAndStatus(anyInt(), any())).thenReturn(1L);

            assertThatThrownBy(() -> teamRecruitmentService.updateRecruitment(
                AUTHOR_ID, RECRUITMENT_ID, request(ROLE_BASED, null, List.of(
                    new UpdateRoleInput(ROLE_ID, "PM", 2)))))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TEAM_RECRUITMENT_ROLE_UPDATE_NOT_ALLOWED);
        }

        @Test
        @DisplayName("역할 정원을 늘릴 때는 지원자 조회 없이 통과한다")
        void canGrowRoleWithApplicants() {
            givenFoundRecruitment();

            teamRecruitmentService.updateRecruitment(AUTHOR_ID, RECRUITMENT_ID, request(ROLE_BASED, null, List.of(
                new UpdateRoleInput(ROLE_ID, "PM", 5))));

            assertThat(recruitment.getRoles().get(0).getMaxParticipants()).isEqualTo(5);
            assertThat(recruitment.getMaxParticipants()).isEqualTo(5);
            verify(applicationRepository, never()).countByRole_IdAndStatus(anyInt(), any());
        }

        @Test
        @DisplayName("거절된 지원서만 있는 역할도 삭제할 수 없다")
        void cannotRemoveRoleWithRejectedApplication() {
            givenFoundRecruitment();
            when(applicationRepository.countByRole_IdAndStatus(anyInt(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1) == REJECTED ? 1L : 0L);

            assertThatThrownBy(() -> teamRecruitmentService.updateRecruitment(
                AUTHOR_ID, RECRUITMENT_ID, request(ROLE_BASED, null, List.of(
                    new UpdateRoleInput(null, "새 역할", 2)))))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TEAM_RECRUITMENT_ROLE_UPDATE_NOT_ALLOWED);
        }

        @Test
        @DisplayName("지원자가 있는 역할은 삭제할 수 없다")
        void cannotRemoveRoleWithApplicants() {
            givenFoundRecruitment();
            when(applicationRepository.countByRole_IdAndStatus(anyInt(), any())).thenReturn(1L);

            assertThatThrownBy(() -> teamRecruitmentService.updateRecruitment(
                AUTHOR_ID, RECRUITMENT_ID, request(ROLE_BASED, null, List.of(
                    new UpdateRoleInput(null, "새 역할", 2)))))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TEAM_RECRUITMENT_ROLE_UPDATE_NOT_ALLOWED);
        }

        @Test
        @DisplayName("기존 역할의 순서는 요청 순서와 무관하게 유지되고 새 역할은 빈 슬롯을 받는다")
        void keepsExistingDisplayOrderAndFillsFreeSlot() {
            recruitment.addRole(role(5, "프론트엔드", 2, 2));
            givenFoundRecruitment();
            givenNoApplicants();

            teamRecruitmentService.updateRecruitment(AUTHOR_ID, RECRUITMENT_ID, request(ROLE_BASED, null, List.of(
                new UpdateRoleInput(5, "프론트엔드", 2),
                new UpdateRoleInput(ROLE_ID, "PM", 3),
                new UpdateRoleInput(null, "디자이너", 1)
            )));

            assertThat(recruitment.getRoles())
                .extracting(TeamRecruitmentRole::getName, TeamRecruitmentRole::getDisplayOrder)
                .containsExactlyInAnyOrder(
                    tuple("PM", 1),
                    tuple("프론트엔드", 2),
                    tuple("디자이너", 3)
                );
            verify(entityManager).flush();
        }

        @Test
        @DisplayName("역할 이름이 임시 이름 후보와 겹쳐도 이름 교환이 성공한다")
        void renamesEvenWhenTemporaryNameCandidatesAreTaken() {
            recruitment.getRoles().clear();
            recruitment.addRole(role(ROLE_ID, "#0", 1, 1));
            recruitment.addRole(role(5, "#1", 2, 2));
            givenFoundRecruitment();
            givenNoApplicants();

            teamRecruitmentService.updateRecruitment(AUTHOR_ID, RECRUITMENT_ID, request(ROLE_BASED, null, List.of(
                new UpdateRoleInput(ROLE_ID, "#1", 1),
                new UpdateRoleInput(5, "#0", 2)
            )));

            assertThat(recruitment.getRoles())
                .extracting(TeamRecruitmentRole::getId, TeamRecruitmentRole::getName)
                .containsExactlyInAnyOrder(tuple(ROLE_ID, "#1"), tuple(5, "#0"));
        }

        @Test
        @DisplayName("삭제로 비는 슬롯을 새 역할이 재사용한다")
        void reusesFreedDisplayOrder() {
            recruitment.addRole(role(5, "프론트엔드", 2, 2));
            givenFoundRecruitment();
            givenNoApplicants();

            teamRecruitmentService.updateRecruitment(AUTHOR_ID, RECRUITMENT_ID, request(ROLE_BASED, null, List.of(
                new UpdateRoleInput(ROLE_ID, "PM", 3),
                new UpdateRoleInput(null, "디자이너", 1)
            )));

            assertThat(recruitment.getRoles())
                .extracting(TeamRecruitmentRole::getName, TeamRecruitmentRole::getDisplayOrder)
                .containsExactlyInAnyOrder(
                    tuple("PM", 1),
                    tuple("디자이너", 2)
                );
            verify(chatRoomRepository, never()).save(any());
        }
    }
}
