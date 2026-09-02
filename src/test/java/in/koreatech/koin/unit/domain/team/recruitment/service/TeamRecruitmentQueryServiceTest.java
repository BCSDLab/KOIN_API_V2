package in.koreatech.koin.unit.domain.team.recruitment.service;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.ACCEPTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.PENDING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplyBlockReason.ALREADY_APPLIED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplyBlockReason.DEADLINE_PASSED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplyBlockReason.LOGIN_REQUIRED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplyBlockReason.OWN_RECRUITMENT;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplyBlockReason.PROFILE_REQUIRED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplyBlockReason.RECRUITMENT_CLOSED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplyBlockReason.ROLE_CLOSED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.CLOSED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType.GENERAL;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType.ROLE_BASED;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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

import in.koreatech.koin.domain.team.recruitment.dto.RecruitmentDetail;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentRole;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentApplicationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentListQueryRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentProfileRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.team.recruitment.service.TeamRecruitmentQueryService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.unit.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class TeamRecruitmentQueryServiceTest {

    private static final Integer AUTHOR_ID = 1;
    private static final Integer VIEWER_ID = 2;
    private static final Integer RECRUITMENT_ID = 17;
    private static final Integer ROLE_ID = 4;
    private static final Integer TEAM_ROOM_ID = 31;
    private static final LocalDate TODAY = LocalDate.of(2026, 8, 30);

    @InjectMocks
    private TeamRecruitmentQueryService teamRecruitmentQueryService;

    @Mock
    private TeamRecruitmentRepository teamRecruitmentRepository;

    @Mock
    private TeamRecruitmentListQueryRepository listQueryRepository;

    @Mock
    private TeamRecruitmentApplicationRepository applicationRepository;

    @Mock
    private TeamRecruitmentProfileRepository profileRepository;

    @Mock
    private TeamRecruitmentChatRoomRepository chatRoomRepository;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-08-30T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    private TeamRecruitment recruitment;

    @BeforeEach
    void setUp() {
        recruitment = recruitment(ROLE_BASED, 3, 0, TODAY.plusDays(3));
    }

    private TeamRecruitment recruitment(
        in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType type,
        int maxParticipants,
        int currentParticipants,
        LocalDate deadline
    ) {
        User author = UserFixture.id_설정_코인_유저(AUTHOR_ID);
        TeamRecruitment created = TeamRecruitment.builder()
            .author(author)
            .category(TeamRecruitmentCategory.CONTEST)
            .title("AI 아이디어 공모전 팀원 모집")
            .meetingType(TeamRecruitmentMeetingType.ONLINE)
            .activityStartDate(TODAY.plusDays(10))
            .activityEndDate(TODAY.plusDays(20))
            .deadlineDate(deadline)
            .recruitmentType(type)
            .maxParticipants(maxParticipants)
            .currentParticipants(currentParticipants)
            .description("공모전 팀원을 모집합니다.")
            .build();
        ReflectionTestUtils.setField(created, "id", RECRUITMENT_ID);
        if (type == ROLE_BASED) {
            created.addRole(role(ROLE_ID, 3, 0));
        }
        return created;
    }

    private static TeamRecruitmentRole role(Integer id, int maxParticipants, int currentParticipants) {
        TeamRecruitmentRole role = TeamRecruitmentRole.builder()
            .name("PM")
            .maxParticipants(maxParticipants)
            .currentParticipants(currentParticipants)
            .displayOrder(1)
            .build();
        ReflectionTestUtils.setField(role, "id", id);
        return role;
    }

    private void givenFound() {
        when(teamRecruitmentRepository.findById(RECRUITMENT_ID)).thenReturn(Optional.of(recruitment));
    }

    private void givenHasProfile(boolean hasProfile) {
        lenient().when(profileRepository.existsByUser_Id(anyInt())).thenReturn(hasProfile);
    }

    private void givenNoApplication() {
        lenient().when(applicationRepository.findByRecruitment_IdAndApplicant_Id(anyInt(), anyInt()))
            .thenReturn(Optional.empty());
    }

    private void givenApplication(TeamRecruitmentApplicationStatus status) {
        TeamRecruitmentApplication application = TeamRecruitmentApplication.builder()
            .recruitment(recruitment)
            .applicant(UserFixture.id_설정_코인_유저(VIEWER_ID))
            .motivation("지원 동기")
            .availability("월수금")
            .status(status)
            .profileSnapshot("{}")
            .snapshotVersion(1)
            .build();
        ReflectionTestUtils.setField(application, "id", 51);
        lenient().when(applicationRepository.findByRecruitment_IdAndApplicant_Id(anyInt(), anyInt()))
            .thenReturn(Optional.of(application));
    }

    private void givenTeamRoom() {
        TeamRecruitmentChatRoom teamRoom = TeamRecruitmentChatRoom.createTeamRoom(recruitment);
        ReflectionTestUtils.setField(teamRoom, "id", TEAM_ROOM_ID);
        lenient().when(chatRoomRepository.findByRecruitment_IdAndRoomScopeKey(anyInt(), anyString()))
            .thenReturn(Optional.of(teamRoom));
    }

    private RecruitmentDetail detail(Integer userId) {
        return teamRecruitmentQueryService.getRecruitment(RECRUITMENT_ID, userId);
    }

    @Test
    @DisplayName("모집글 인원은 작성자를 제외한 승인 지원자 1명 중 1명이다")
    void recruitmentParticipantCountExcludesAuthor() {
        recruitment = recruitment(GENERAL, 1, 1, TODAY.plusDays(3));
        givenFound();

        RecruitmentDetail response = detail(null);

        assertThat(response)
            .extracting(RecruitmentDetail::currentParticipants, RecruitmentDetail::maxParticipants)
            .containsExactly(1, 1);
    }

    @Nested
    @DisplayName("지원 불가 사유")
    class ApplyBlockReason {

        @Test
        @DisplayName("비로그인 조회는 LOGIN_REQUIRED 이다")
        void loginRequired() {
            givenFound();

            RecruitmentDetail response = detail(null);

            assertThat(response.applyBlockReason()).isEqualTo(LOGIN_REQUIRED);
            assertThat(response.canApply()).isFalse();
        }

        @Test
        @DisplayName("작성자 조회는 OWN_RECRUITMENT 이다")
        void ownRecruitment() {
            givenFound();
            givenNoApplication();

            assertThat(detail(AUTHOR_ID).applyBlockReason()).isEqualTo(OWN_RECRUITMENT);
        }

        @Test
        @DisplayName("마감된 모집글은 RECRUITMENT_CLOSED 이다")
        void recruitmentClosed() {
            ReflectionTestUtils.setField(recruitment, "status", CLOSED);
            givenFound();
            givenNoApplication();

            assertThat(detail(VIEWER_ID).applyBlockReason()).isEqualTo(RECRUITMENT_CLOSED);
        }

        @Test
        @DisplayName("지원 마감일이 지나면 DEADLINE_PASSED 이다")
        void deadlinePassed() {
            recruitment = recruitment(ROLE_BASED, 3, 0, TODAY.minusDays(1));
            givenFound();
            givenNoApplication();

            assertThat(detail(VIEWER_ID).applyBlockReason()).isEqualTo(DEADLINE_PASSED);
        }

        @Test
        @DisplayName("이미 지원했으면 ALREADY_APPLIED 이다")
        void alreadyApplied() {
            givenFound();
            givenApplication(PENDING);

            assertThat(detail(VIEWER_ID).applyBlockReason()).isEqualTo(ALREADY_APPLIED);
        }

        @Test
        @DisplayName("프로필이 없으면 PROFILE_REQUIRED 이다")
        void profileRequired() {
            givenFound();
            givenNoApplication();
            givenHasProfile(false);

            assertThat(detail(VIEWER_ID).applyBlockReason()).isEqualTo(PROFILE_REQUIRED);
        }

        @Test
        @DisplayName("모든 역할이 마감되면 ROLE_CLOSED 이다")
        void roleClosed() {
            recruitment.getRoles().clear();
            recruitment.addRole(role(ROLE_ID, 1, 1));
            givenFound();
            givenNoApplication();
            givenHasProfile(true);

            assertThat(detail(VIEWER_ID).applyBlockReason()).isEqualTo(ROLE_CLOSED);
        }

        @Test
        @DisplayName("역할 구분이 없는 모집은 전체 정원이 차면 ROLE_CLOSED 이다")
        void generalCapacityFull() {
            recruitment = recruitment(GENERAL, 2, 2, TODAY.plusDays(3));
            givenFound();
            givenNoApplication();
            givenHasProfile(true);

            assertThat(detail(VIEWER_ID).applyBlockReason()).isEqualTo(ROLE_CLOSED);
        }

        @Test
        @DisplayName("지원 가능하면 사유가 null 이고 can_apply 가 true 이다")
        void canApply() {
            givenFound();
            givenNoApplication();
            givenHasProfile(true);

            RecruitmentDetail response = detail(VIEWER_ID);

            assertThat(response.applyBlockReason()).isNull();
            assertThat(response.canApply()).isTrue();
        }
    }

    @Nested
    @DisplayName("사유 우선순위는 합의된 화면 안내 순서를 따른다")
    class ReasonPriority {

        @Test
        @DisplayName("비로그인 작성자 조회는 LOGIN_REQUIRED 가 먼저다")
        void loginBeforeOwn() {
            givenFound();

            assertThat(detail(null).applyBlockReason()).isEqualTo(LOGIN_REQUIRED);
        }

        @Test
        @DisplayName("작성자가 마감한 글을 봐도 OWN_RECRUITMENT 가 먼저다")
        void ownBeforeClosed() {
            ReflectionTestUtils.setField(recruitment, "status", CLOSED);
            givenFound();
            givenNoApplication();

            assertThat(detail(AUTHOR_ID).applyBlockReason()).isEqualTo(OWN_RECRUITMENT);
        }

        @Test
        @DisplayName("이미 지원한 마감 글은 ALREADY_APPLIED 가 먼저다")
        void alreadyAppliedBeforeClosed() {
            ReflectionTestUtils.setField(recruitment, "status", CLOSED);
            givenFound();
            givenApplication(PENDING);

            assertThat(detail(VIEWER_ID).applyBlockReason()).isEqualTo(ALREADY_APPLIED);
        }

        @Test
        @DisplayName("이미 지원했고 마감일이 지났어도 ALREADY_APPLIED 가 먼저다")
        void alreadyAppliedBeforeDeadline() {
            recruitment = recruitment(ROLE_BASED, 3, 0, TODAY.minusDays(1));
            givenFound();
            givenApplication(PENDING);

            assertThat(detail(VIEWER_ID).applyBlockReason()).isEqualTo(ALREADY_APPLIED);
        }

        @Test
        @DisplayName("마감된 글에서 마감일도 지났으면 RECRUITMENT_CLOSED 가 먼저다")
        void closedBeforeDeadline() {
            recruitment = recruitment(ROLE_BASED, 3, 0, TODAY.minusDays(1));
            ReflectionTestUtils.setField(recruitment, "status", CLOSED);
            givenFound();
            givenNoApplication();

            assertThat(detail(VIEWER_ID).applyBlockReason()).isEqualTo(RECRUITMENT_CLOSED);
        }

        @Test
        @DisplayName("프로필이 없고 역할이 마감됐으면 ROLE_CLOSED 가 먼저다")
        void roleClosedBeforeProfile() {
            recruitment.getRoles().clear();
            recruitment.addRole(role(ROLE_ID, 1, 1));
            givenFound();
            givenNoApplication();
            givenHasProfile(false);

            assertThat(detail(VIEWER_ID).applyBlockReason()).isEqualTo(ROLE_CLOSED);
        }
    }

    @Nested
    @DisplayName("팀 채팅방 정보")
    class TeamChatRoom {

        @Test
        @DisplayName("작성자는 팀 채팅방 정보를 받는다")
        void authorReceivesTeamRoom() {
            givenFound();
            givenNoApplication();
            givenTeamRoom();

            RecruitmentDetail response = detail(AUTHOR_ID);

            assertThat(response.isAuthor()).isTrue();
            assertThat(response.canManageApplicants()).isTrue();
            assertThat(response.teamChatAvailable()).isTrue();
            assertThat(response.teamChatRoomId()).isEqualTo(TEAM_ROOM_ID);
        }

        @Test
        @DisplayName("승인된 지원자는 팀 채팅방 정보를 받는다")
        void acceptedApplicantReceivesTeamRoom() {
            givenFound();
            givenApplication(ACCEPTED);
            givenTeamRoom();

            RecruitmentDetail response = detail(VIEWER_ID);

            assertThat(response.teamChatAvailable()).isTrue();
            assertThat(response.teamChatRoomId()).isEqualTo(TEAM_ROOM_ID);
            assertThat(response.application().status()).isEqualTo(ACCEPTED);
        }

        @Test
        @DisplayName("대기 중인 지원자는 팀 채팅방 정보를 받지 못한다")
        void pendingApplicantHasNoTeamRoom() {
            givenFound();
            givenApplication(PENDING);

            RecruitmentDetail response = detail(VIEWER_ID);

            assertThat(response.teamChatAvailable()).isFalse();
            assertThat(response.teamChatRoomId()).isNull();
        }

        @Test
        @DisplayName("비로그인 조회는 팀 채팅방 정보를 받지 못한다")
        void anonymousHasNoTeamRoom() {
            givenFound();

            RecruitmentDetail response = detail(null);

            assertThat(response.teamChatAvailable()).isFalse();
            assertThat(response.teamChatRoomId()).isNull();
            assertThat(response.application()).isNull();
        }
    }

    @Nested
    @DisplayName("상세 조회 실패")
    class DetailFailure {

        @Test
        @DisplayName("존재하지 않는 모집글은 404 이다")
        void notFound() {
            when(teamRecruitmentRepository.findById(RECRUITMENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> detail(VIEWER_ID))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TEAM_RECRUITMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("삭제된 모집글은 404 이므로 RECRUITMENT_DELETED 사유는 노출되지 않는다")
        void deletedRecruitmentReturnsNotFound() {
            recruitment.markDeleted(java.time.LocalDateTime.of(2026, 8, 30, 0, 0));
            givenFound();

            assertThatThrownBy(() -> detail(VIEWER_ID))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TEAM_RECRUITMENT_NOT_FOUND);
        }
    }
}
