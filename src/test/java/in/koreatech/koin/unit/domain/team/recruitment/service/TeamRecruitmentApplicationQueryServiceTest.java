package in.koreatech.koin.unit.domain.team.recruitment.service;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.ACCEPTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.PENDING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.REJECTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus.ACTIVE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType.DIRECT;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType.TEAM;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.CLOSED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.RECRUITING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType.GENERAL;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.team.recruitment.dto.ApplicantListResponse;
import in.koreatech.koin.domain.team.recruitment.dto.ApplicantSummary;
import in.koreatech.koin.domain.team.recruitment.dto.MyApplication;
import in.koreatech.koin.domain.team.recruitment.dto.MyApplicationListResponse;
import in.koreatech.koin.domain.team.recruitment.dto.RecruitmentCard;
import in.koreatech.koin.domain.team.recruitment.dto.RecruitmentRole;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationSort;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentRole;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentApplicationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.team.recruitment.service.TeamRecruitmentApplicationQueryService;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.global.exception.custom.KoinIllegalStateException;
import in.koreatech.koin.unit.fixture.UserFixture;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class TeamRecruitmentApplicationQueryServiceTest {

    private static final Integer APPLICANT_ID = 2;
    private static final Integer AUTHOR_ID = 1;
    private static final Integer RECRUITMENT_ID = 10;
    private static final Integer TEAM_ROOM_ID = 40;
    private static final Integer DIRECT_ROOM_ID = 41;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Mock
    private TeamRecruitmentRepository recruitmentRepository;

    @Mock
    private TeamRecruitmentApplicationRepository applicationRepository;

    @Mock
    private TeamRecruitmentChatRoomRepository chatRoomRepository;

    @Mock
    private StudentRepository studentRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private Clock clock;

    @InjectMocks
    private TeamRecruitmentApplicationQueryService queryService;

    @BeforeEach
    void setUpClock() {
        lenient().when(clock.instant()).thenReturn(Instant.parse("2026-08-28T03:00:00Z"));
        lenient().when(clock.getZone()).thenReturn(KST);
        lenient().when(clock.withZone(KST)).thenReturn(clock);
    }

    @Test
    void PENDING과_REJECTED는_TEAM_room이_있어도_팀채팅을_사용할_수_없고_ACCEPTED만_사용할_수_있다() {
        TeamRecruitment recruitment = recruitment();
        TeamRecruitmentApplication pending = application(20, recruitment, PENDING);
        TeamRecruitmentApplication rejected = application(21, recruitment, REJECTED);
        TeamRecruitmentApplication accepted = application(22, recruitment, ACCEPTED);
        TeamRecruitmentChatRoom teamRoom = TeamRecruitmentChatRoom.builder()
            .id(TEAM_ROOM_ID)
            .recruitment(recruitment)
            .roomScopeKey("TEAM")
            .roomType(TEAM)
            .status(ACTIVE)
            .build();
        TeamRecruitmentChatRoom directRoom = TeamRecruitmentChatRoom.builder()
            .id(DIRECT_ROOM_ID)
            .recruitment(recruitment)
            .roomScopeKey("DIRECT:22")
            .roomType(DIRECT)
            .application(accepted)
            .status(ACTIVE)
            .build();
        when(studentRepository.getById(APPLICANT_ID)).thenReturn(null);
        when(applicationRepository.countByApplicant_IdAndStatusIn(eq(APPLICANT_ID), any()))
            .thenReturn(3L);
        when(applicationRepository.findAllByApplicant_IdAndStatusIn(
            eq(APPLICANT_ID),
            any(),
            any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(pending, rejected, accepted)));
        when(chatRoomRepository.findByRecruitment_IdAndRoomScopeKey(RECRUITMENT_ID, "TEAM"))
            .thenReturn(Optional.of(teamRoom));
        when(chatRoomRepository.findByRecruitment_IdAndApplication_IdAndRoomType(any(), any(), any()))
            .thenReturn(Optional.of(directRoom));

        MyApplicationListResponse response = queryService.getMyApplications(
            null,
            TeamRecruitmentApplicationSort.LATEST_DESC,
            1,
            10,
            APPLICANT_ID
        );

        assertThat(response.applications()).hasSize(3);
        MyApplication pendingResponse = response.applications().get(0);
        MyApplication rejectedResponse = response.applications().get(1);
        MyApplication acceptedResponse = response.applications().get(2);
        assertThat(pendingResponse.teamChatAvailable()).isFalse();
        assertThat(pendingResponse.teamChatRoomId()).isNull();
        assertThat(rejectedResponse.teamChatAvailable()).isFalse();
        assertThat(rejectedResponse.teamChatRoomId()).isNull();
        assertThat(pendingResponse.directChatRoomId()).isNull();
        assertThat(rejectedResponse.directChatRoomId()).isNull();
        assertThat(acceptedResponse.teamChatAvailable()).isTrue();
        assertThat(acceptedResponse.teamChatRoomId()).isEqualTo(TEAM_ROOM_ID);
        assertThat(acceptedResponse.directChatRoomId()).isEqualTo(DIRECT_ROOM_ID);
    }

    @Test
    void KST_마감일이_지난_RECRUITING_모집글은_두_목록에서_CLOSED와_전체_역할_마감으로_응답된다() {
        TeamRecruitment recruitment = recruitment(LocalDate.of(2026, 8, 27));
        recruitment.addRole(role(50, "백엔드", 1, 1, 1));
        recruitment.addRole(role(51, "프론트엔드", 2, 0, 2));
        TeamRecruitmentApplication application = applicationWithSnapshot(20, recruitment, PENDING);

        // 2026-08-27T15:30Z is 2026-08-28T00:30 in KST, so the 2026-08-27
        // deadline is past even though the UTC calendar date is still the 27th.
        when(clock.instant()).thenReturn(Instant.parse("2026-08-27T15:30:00Z"));
        when(studentRepository.getById(APPLICANT_ID)).thenReturn(null);
        when(applicationRepository.countByApplicant_IdAndStatusIn(eq(APPLICANT_ID), any()))
            .thenReturn(1L);
        when(applicationRepository.findAllByApplicant_IdAndStatusIn(
            eq(APPLICANT_ID),
            any(),
            any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(application)));

        MyApplicationListResponse myResponse = queryService.getMyApplications(
            List.of(PENDING),
            TeamRecruitmentApplicationSort.LATEST_DESC,
            1,
            10,
            APPLICANT_ID
        );

        RecruitmentCard card = myResponse.applications().get(0).recruitment();
        assertThat(card.status()).isEqualTo(CLOSED);
        assertThat(card.dDay()).isNull();
        assertThat(card.roles()).extracting(RecruitmentRole::isClosed).containsExactly(true, true);

        when(studentRepository.getById(AUTHOR_ID)).thenReturn(null);
        when(recruitmentRepository.findById(RECRUITMENT_ID)).thenReturn(Optional.of(recruitment));
        when(applicationRepository.countByRecruitment_IdAndStatusIn(eq(RECRUITMENT_ID), any()))
            .thenReturn(1L);
        when(applicationRepository.findAllByRecruitment_IdAndStatusIn(
            eq(RECRUITMENT_ID),
            any(),
            any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(application)));
        when(chatRoomRepository.findByRecruitment_IdAndRoomScopeKey(RECRUITMENT_ID, "TEAM"))
            .thenReturn(Optional.of(teamRoom(recruitment)));

        ApplicantListResponse authorResponse = queryService.getApplications(
            RECRUITMENT_ID,
            List.of(PENDING),
            1,
            10,
            AUTHOR_ID
        );

        assertThat(authorResponse.recruitment().status()).isEqualTo(CLOSED);
        assertThat(authorResponse.recruitment().dDay()).isNull();
        assertThat(authorResponse.recruitment().roles())
            .extracting(RecruitmentRole::isClosed)
            .containsExactly(true, true);
    }

    @Test
    void 역할_하나가_정원에_도달해도_마감_전_모집글은_RECRUITING이고_해당_역할만_닫힌다() {
        TeamRecruitment recruitment = recruitment();
        recruitment.addRole(role(50, "백엔드", 1, 1, 1));
        recruitment.addRole(role(51, "프론트엔드", 2, 0, 2));
        TeamRecruitmentApplication application = applicationWithSnapshot(20, recruitment, PENDING);

        when(studentRepository.getById(APPLICANT_ID)).thenReturn(null);
        when(applicationRepository.countByApplicant_IdAndStatusIn(eq(APPLICANT_ID), any()))
            .thenReturn(1L);
        when(applicationRepository.findAllByApplicant_IdAndStatusIn(
            eq(APPLICANT_ID),
            any(),
            any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(application)));

        MyApplicationListResponse myResponse = queryService.getMyApplications(
            List.of(PENDING),
            TeamRecruitmentApplicationSort.LATEST_DESC,
            1,
            10,
            APPLICANT_ID
        );

        RecruitmentCard card = myResponse.applications().get(0).recruitment();
        assertThat(card.status()).isEqualTo(RECRUITING);
        assertThat(card.dDay()).isEqualTo(3);
        assertThat(card.roles()).extracting(RecruitmentRole::isClosed).containsExactly(true, false);

        when(studentRepository.getById(AUTHOR_ID)).thenReturn(null);
        when(recruitmentRepository.findById(RECRUITMENT_ID)).thenReturn(Optional.of(recruitment));
        when(applicationRepository.countByRecruitment_IdAndStatusIn(eq(RECRUITMENT_ID), any()))
            .thenReturn(1L);
        when(applicationRepository.findAllByRecruitment_IdAndStatusIn(
            eq(RECRUITMENT_ID),
            any(),
            any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(application)));
        when(chatRoomRepository.findByRecruitment_IdAndRoomScopeKey(RECRUITMENT_ID, "TEAM"))
            .thenReturn(Optional.of(teamRoom(recruitment)));

        ApplicantListResponse authorResponse = queryService.getApplications(
            RECRUITMENT_ID,
            List.of(PENDING),
            1,
            10,
            AUTHOR_ID
        );

        assertThat(authorResponse.recruitment().status()).isEqualTo(RECRUITING);
        assertThat(authorResponse.recruitment().dDay()).isEqualTo(3);
        assertThat(authorResponse.recruitment().roles())
            .extracting(RecruitmentRole::isClosed)
            .containsExactly(true, false);
    }

    @Test
    void 내_지원_목록의_초과_페이지는_마지막_페이지로_보정되어_조회된다() {
        TeamRecruitment recruitment = recruitment();
        TeamRecruitmentApplication pending = application(20, recruitment, PENDING);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(studentRepository.getById(APPLICANT_ID)).thenReturn(null);
        when(applicationRepository.countByApplicant_IdAndStatusIn(eq(APPLICANT_ID), any()))
            .thenReturn(21L);
        when(applicationRepository.findAllByApplicant_IdAndStatusIn(
            eq(APPLICANT_ID),
            any(),
            any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(pending)));

        MyApplicationListResponse response = queryService.getMyApplications(
            List.of(PENDING),
            TeamRecruitmentApplicationSort.LATEST_DESC,
            99,
            10,
            APPLICANT_ID
        );

        verify(applicationRepository).findAllByApplicant_IdAndStatusIn(
            eq(APPLICANT_ID),
            eq(List.of(PENDING)),
            pageableCaptor.capture()
        );
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
        assertThat(response.totalCount()).isEqualTo(21L);
        assertThat(response.currentCount()).isEqualTo(1);
        assertThat(response.totalPage()).isEqualTo(3);
        assertThat(response.currentPage()).isEqualTo(3);
    }

    @Test
    void 작성자_지원자_목록은_지원_당시_snapshot의_프로필을_사용한다() {
        TeamRecruitment recruitment = recruitment();
        TeamRecruitmentApplication application = application(20, recruitment, PENDING);
        application = TeamRecruitmentApplication.builder()
            .id(application.getId())
            .recruitment(recruitment)
            .applicant(application.getApplicant())
            .motivation(application.getMotivation())
            .availability(application.getAvailability())
            .status(PENDING)
            .profileSnapshot(
                "{\"nickname\":\"지원 당시 이름\","
                    + "\"department\":\"컴퓨터공학부\","
                    + "\"student_year\":2023,"
                    + "\"preferred_role\":\"백엔드\","
                    + "\"skills\":[],\"activities\":[],"
                    + "\"self_introduction\":\"지원 당시 소개\"}"
            )
            .build();

        when(studentRepository.getById(AUTHOR_ID)).thenReturn(null);
        when(recruitmentRepository.findById(RECRUITMENT_ID)).thenReturn(Optional.of(recruitment));
        when(applicationRepository.countByRecruitment_IdAndStatusIn(eq(RECRUITMENT_ID), any()))
            .thenReturn(1L);
        when(applicationRepository.findAllByRecruitment_IdAndStatusIn(
            eq(RECRUITMENT_ID),
            any(),
            any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(application)));
        when(chatRoomRepository.findByRecruitment_IdAndRoomScopeKey(RECRUITMENT_ID, "TEAM"))
            .thenReturn(Optional.of(teamRoom(recruitment)));

        ApplicantListResponse response = queryService.getApplications(
            RECRUITMENT_ID,
            null,
            1,
            10,
            AUTHOR_ID
        );

        ApplicantSummary summary = response.applications().get(0);
        assertThat(summary.nickname()).isEqualTo("지원 당시 이름");
        assertThat(summary.department()).isEqualTo("컴퓨터공학부");
        assertThat(summary.studentYear()).isEqualTo(2023);
        assertThat(summary.status()).isEqualTo(PENDING);
        assertThat(summary.canOpenDirectChat()).isFalse();
    }

    @Test
    void 작성자_지원자_목록의_초과_페이지는_마지막_페이지로_보정되어_조회된다() {
        TeamRecruitment recruitment = recruitment();
        TeamRecruitmentApplication pending = TeamRecruitmentApplication.builder()
            .id(20)
            .recruitment(recruitment)
            .applicant(UserFixture.id_설정_코인_유저(APPLICANT_ID))
            .motivation("지원 동기")
            .availability("가능")
            .status(PENDING)
            .profileSnapshot(
                "{\"nickname\":\"지원 당시 이름\","
                    + "\"department\":\"컴퓨터공학부\","
                    + "\"student_year\":2023,"
                    + "\"preferred_role\":\"백엔드\","
                    + "\"skills\":[],\"activities\":[],"
                    + "\"self_introduction\":\"지원 당시 소개\"}"
            )
            .build();
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(studentRepository.getById(AUTHOR_ID)).thenReturn(null);
        when(recruitmentRepository.findById(RECRUITMENT_ID)).thenReturn(Optional.of(recruitment));
        when(applicationRepository.countByRecruitment_IdAndStatusIn(eq(RECRUITMENT_ID), any()))
            .thenReturn(21L);
        when(applicationRepository.findAllByRecruitment_IdAndStatusIn(
            eq(RECRUITMENT_ID),
            any(),
            any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(pending)));
        when(chatRoomRepository.findByRecruitment_IdAndRoomScopeKey(RECRUITMENT_ID, "TEAM"))
            .thenReturn(Optional.of(teamRoom(recruitment)));

        ApplicantListResponse response = queryService.getApplications(
            RECRUITMENT_ID,
            List.of(PENDING),
            99,
            10,
            AUTHOR_ID
        );

        verify(applicationRepository).findAllByRecruitment_IdAndStatusIn(
            eq(RECRUITMENT_ID),
            eq(List.of(PENDING)),
            pageableCaptor.capture()
        );
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
        assertThat(response.totalCount()).isEqualTo(21L);
        assertThat(response.currentCount()).isEqualTo(1);
        assertThat(response.totalPage()).isEqualTo(3);
        assertThat(response.currentPage()).isEqualTo(3);
    }

    @Test
    void ACCEPTED인데_TEAM_room이_없으면_내부_무결성_예외가_발생한다() {
        TeamRecruitment recruitment = recruitment();
        TeamRecruitmentApplication accepted = application(22, recruitment, ACCEPTED);

        when(studentRepository.getById(APPLICANT_ID)).thenReturn(null);
        when(applicationRepository.countByApplicant_IdAndStatusIn(eq(APPLICANT_ID), any()))
            .thenReturn(1L);
        when(applicationRepository.findAllByApplicant_IdAndStatusIn(
            eq(APPLICANT_ID),
            any(),
            any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(accepted)));
        when(chatRoomRepository.findByRecruitment_IdAndRoomScopeKey(RECRUITMENT_ID, "TEAM"))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> queryService.getMyApplications(
            List.of(ACCEPTED),
            TeamRecruitmentApplicationSort.LATEST_DESC,
            1,
            10,
            APPLICANT_ID
        ))
            .isInstanceOf(KoinIllegalStateException.class)
            .hasMessageContaining("TEAM 채팅방이 없습니다")
            .hasMessageContaining("recruitmentId: 10")
            .hasMessageContaining("applicationId: 22");
    }

    @Test
    void 작성자_지원자_목록에_TEAM_room이_없으면_내부_무결성_예외가_발생한다() {
        TeamRecruitment recruitment = recruitment();

        when(studentRepository.getById(AUTHOR_ID)).thenReturn(null);
        when(recruitmentRepository.findById(RECRUITMENT_ID)).thenReturn(Optional.of(recruitment));
        when(applicationRepository.countByRecruitment_IdAndStatusIn(eq(RECRUITMENT_ID), any()))
            .thenReturn(0L);
        when(applicationRepository.findAllByRecruitment_IdAndStatusIn(
            eq(RECRUITMENT_ID),
            any(),
            any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));
        when(chatRoomRepository.findByRecruitment_IdAndRoomScopeKey(RECRUITMENT_ID, "TEAM"))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> queryService.getApplications(
            RECRUITMENT_ID,
            null,
            1,
            10,
            AUTHOR_ID
        ))
            .isInstanceOf(KoinIllegalStateException.class)
            .hasMessageContaining("팀원 모집글에 TEAM 채팅방이 없습니다")
            .hasMessageContaining("recruitmentId: 10");
    }

    @Test
    void 삭제된_모집글의_지원자_상세는_모집글_없음으로_응답된다() {
        TeamRecruitment deletedRecruitment = recruitment();
        deletedRecruitment.markDeleted(LocalDateTime.of(2026, 8, 28, 12, 0));

        when(studentRepository.getById(AUTHOR_ID)).thenReturn(null);
        when(recruitmentRepository.findById(RECRUITMENT_ID))
            .thenReturn(Optional.of(deletedRecruitment));

        assertThatThrownBy(() -> queryService.getApplicationDetail(
            RECRUITMENT_ID,
            20,
            AUTHOR_ID
        ))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", TEAM_RECRUITMENT_NOT_FOUND);
    }

    private TeamRecruitmentChatRoom teamRoom(TeamRecruitment recruitment) {
        return TeamRecruitmentChatRoom.builder()
            .id(TEAM_ROOM_ID)
            .recruitment(recruitment)
            .roomScopeKey("TEAM")
            .roomType(TEAM)
            .status(ACTIVE)
            .build();
    }

    private TeamRecruitment recruitment() {
        return recruitment(LocalDate.of(2026, 8, 31));
    }

    private TeamRecruitment recruitment(LocalDate deadlineDate) {
        return TeamRecruitment.builder()
            .id(RECRUITMENT_ID)
            .author(UserFixture.id_설정_코인_유저(1))
            .title("팀원 모집")
            .activityStartDate(LocalDate.of(2026, 9, 1))
            .activityEndDate(LocalDate.of(2026, 9, 30))
            .deadlineDate(deadlineDate)
            .recruitmentType(GENERAL)
            .maxParticipants(5)
            .currentParticipants(0)
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
            .applicant(UserFixture.id_설정_코인_유저(APPLICANT_ID))
            .motivation("지원 동기")
            .availability("가능")
            .status(status)
            .profileSnapshot("{}")
            .build();
    }

    private TeamRecruitmentApplication applicationWithSnapshot(
        Integer id,
        TeamRecruitment recruitment,
        TeamRecruitmentApplicationStatus status
    ) {
        return TeamRecruitmentApplication.builder()
            .id(id)
            .recruitment(recruitment)
            .applicant(UserFixture.id_설정_코인_유저(APPLICANT_ID))
            .motivation("지원 동기")
            .availability("가능")
            .status(status)
            .profileSnapshot(
                "{\"nickname\":\"지원 당시 이름\","
                    + "\"department\":\"컴퓨터공학부\","
                    + "\"student_year\":2023,"
                    + "\"preferred_role\":\"백엔드\","
                    + "\"skills\":[],\"activities\":[],"
                    + "\"self_introduction\":\"지원 당시 소개\"}"
            )
            .build();
    }

    private TeamRecruitmentRole role(
        Integer id,
        String name,
        Integer maxParticipants,
        Integer currentParticipants,
        Integer displayOrder
    ) {
        return TeamRecruitmentRole.builder()
            .id(id)
            .name(name)
            .maxParticipants(maxParticipants)
            .currentParticipants(currentParticipants)
            .displayOrder(displayOrder)
            .build();
    }
}
