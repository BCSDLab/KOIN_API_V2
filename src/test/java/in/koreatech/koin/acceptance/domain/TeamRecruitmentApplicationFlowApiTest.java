package in.koreatech.koin.acceptance.domain;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.ACCEPTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.PENDING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory.PROJECT;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus.ACTIVE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType.TEAM;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType.ONLINE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.CLOSED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.RECRUITING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType.GENERAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMember;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentProfile;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentApplicationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMemberRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentProfileRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;

class TeamRecruitmentApplicationFlowApiTest extends AcceptanceTest {

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    @Autowired
    private TeamRecruitmentRepository recruitmentRepository;

    @Autowired
    private TeamRecruitmentProfileRepository profileRepository;

    @Autowired
    private TeamRecruitmentApplicationRepository applicationRepository;

    @Autowired
    private TeamRecruitmentChatRoomRepository chatRoomRepository;

    @Autowired
    private TeamRecruitmentChatMemberRepository chatMemberRepository;

    private Student author;
    private Student applicant;
    private String authorToken;
    private String applicantToken;
    private RecruitmentContext recruitmentContext;

    @BeforeEach
    void setUp() {
        clear();
        Department department = departmentFixture.컴퓨터공학부();
        author = userFixture.준호_학생(department, null);
        applicant = userFixture.성빈_학생(department);
        authorToken = userFixture.getToken(author.getUser());
        applicantToken = userFixture.getToken(applicant.getUser());
        profileRepository.save(TeamRecruitmentProfile.builder()
            .user(applicant.getUser())
            .profileNickname("지원자")
            .preferredRole("백엔드")
            .selfIntroduction("소개")
            .build());
        recruitmentContext = saveRecruitmentAndTeamRoom("팀원 모집", 2);
    }

    @Test
    void 모집글과_TEAM_방이_생성된_상태에서_지원하고_승인하면_TEAM_방에_참여한다() throws Exception {
        TeamRecruitment recruitment = recruitmentContext.recruitment();
        TeamRecruitmentChatRoom teamRoom = recruitmentContext.teamRoom();

        apply(recruitment, applicantToken)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.recruitment_id").value(recruitment.getId()))
            .andExpect(jsonPath("$.status").value("PENDING"));

        TeamRecruitmentApplication application = applicationRepository
            .findByRecruitment_IdAndApplicant_Id(recruitment.getId(), applicant.getUser().getId())
            .orElseThrow();

        accept(recruitment, application, authorToken)
            .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/team-recruitments/me/applications")
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.applications[0].application_id").value(application.getId()))
            .andExpect(jsonPath("$.applications[0].status").value("ACCEPTED"))
            .andExpect(jsonPath("$.applications[0].team_chat_available").value(true))
            .andExpect(jsonPath("$.applications[0].team_chat_room_id").value(teamRoom.getId()));

        assertThat(applicationRepository.findById(application.getId()).orElseThrow().getStatus())
            .isEqualTo(ACCEPTED);
        assertThat(chatMemberRepository.existsByChatRoom_IdAndUser_Id(teamRoom.getId(), author.getUser().getId()))
            .isTrue();
        assertThat(chatMemberRepository.existsByChatRoom_IdAndUser_Id(teamRoom.getId(), applicant.getUser().getId()))
            .isTrue();
    }

    @Test
    void 마지막_지원자를_승인하면_모집글은_마감되고_TEAM_방은_ACTIVE를_유지한다() throws Exception {
        RecruitmentContext lastSeatContext = saveRecruitmentAndTeamRoom("한 명 모집", 1);
        TeamRecruitment recruitment = lastSeatContext.recruitment();
        TeamRecruitmentChatRoom teamRoom = lastSeatContext.teamRoom();

        apply(recruitment, applicantToken)
            .andExpect(status().isCreated());

        TeamRecruitmentApplication application = applicationRepository
            .findByRecruitment_IdAndApplicant_Id(recruitment.getId(), applicant.getUser().getId())
            .orElseThrow();

        accept(recruitment, application, authorToken)
            .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();

        TeamRecruitment closedRecruitment = recruitmentRepository.findById(recruitment.getId()).orElseThrow();
        TeamRecruitmentChatRoom activeTeamRoom = chatRoomRepository.findById(teamRoom.getId()).orElseThrow();
        assertThat(closedRecruitment.getStatus()).isEqualTo(CLOSED);
        assertThat(closedRecruitment.getCurrentParticipants()).isOne();
        assertThat(activeTeamRoom.getStatus()).isEqualTo(ACTIVE);
        assertThat(chatMemberRepository.existsByChatRoom_IdAndUser_Id(teamRoom.getId(), applicant.getUser().getId()))
            .isTrue();

        mockMvc.perform(get("/team-recruitments/me/applications")
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.applications[0].recruitment.status").value("CLOSED"))
            .andExpect(jsonPath("$.applications[0].team_chat_available").value(true))
            .andExpect(jsonPath("$.applications[0].team_chat_room_id").value(teamRoom.getId()));
    }

    @Test
    void 다른_모집글의_applicationId로_조회하거나_상태를_변경할_수_없다() throws Exception {
        RecruitmentContext otherContext = saveRecruitmentAndTeamRoom("다른 팀원 모집", 2);
        TeamRecruitmentApplication otherApplication = savePendingApplication(otherContext.recruitment());
        Integer recruitmentId = recruitmentContext.recruitment().getId();

        mockMvc.perform(get("/team-recruitments/{recruitmentId}/applications/{applicationId}",
                recruitmentId, otherApplication.getId())
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("TEAM_RECRUITMENT_APPLICATION_NOT_FOUND"));

        accept(recruitmentContext.recruitment(), otherApplication, authorToken)
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("TEAM_RECRUITMENT_APPLICATION_NOT_FOUND"));

        entityManager.clear();
        assertThat(applicationRepository.findById(otherApplication.getId()).orElseThrow().getStatus())
            .isEqualTo(PENDING);
    }

    @Test
    void 로그인하지_않으면_작성자용_지원자_API를_사용할_수_없다() throws Exception {
        TeamRecruitmentApplication application = savePendingApplication(recruitmentContext.recruitment());

        mockMvc.perform(get("/team-recruitments/{recruitmentId}/applications",
                recruitmentContext.recruitment().getId()))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/team-recruitments/{recruitmentId}/applications/{applicationId}",
                recruitmentContext.recruitment().getId(), application.getId()))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/team-recruitments/{recruitmentId}/applications/{applicationId}/status",
                recruitmentContext.recruitment().getId(), application.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "status": "ACCEPTED"
                    }
                    """))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void 작성자가_아니면_작성자용_지원자_API를_사용할_수_없다() throws Exception {
        TeamRecruitmentApplication application = savePendingApplication(recruitmentContext.recruitment());

        mockMvc.perform(get("/team-recruitments/{recruitmentId}/applications",
                recruitmentContext.recruitment().getId())
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("TEAM_RECRUITMENT_FORBIDDEN"));

        mockMvc.perform(get("/team-recruitments/{recruitmentId}/applications/{applicationId}",
                recruitmentContext.recruitment().getId(), application.getId())
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("TEAM_RECRUITMENT_FORBIDDEN"));

        accept(recruitmentContext.recruitment(), application, applicantToken)
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("TEAM_RECRUITMENT_FORBIDDEN"));

        entityManager.clear();
        assertThat(applicationRepository.findById(application.getId()).orElseThrow().getStatus())
            .isEqualTo(PENDING);
    }

    private ResultActions apply(
        TeamRecruitment recruitment,
        String token
    ) throws Exception {
        return mockMvc.perform(post("/team-recruitments/{recruitmentId}/applications", recruitment.getId())
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "role_id": null,
                  "motivation": "지원 동기",
                  "availability": "월수금 20시 이후"
                }
                """));
    }

    private ResultActions accept(
        TeamRecruitment recruitment,
        TeamRecruitmentApplication application,
        String token
    ) throws Exception {
        return mockMvc.perform(put("/team-recruitments/{recruitmentId}/applications/{applicationId}/status",
                recruitment.getId(), application.getId())
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "status": "ACCEPTED"
                }
                """));
    }

    private RecruitmentContext saveRecruitmentAndTeamRoom(String title, int maxParticipants) {
        TeamRecruitment recruitment = recruitmentRepository.save(TeamRecruitment.builder()
            .author(author.getUser())
            .category(PROJECT)
            .title(title)
            .meetingType(ONLINE)
            .activityStartDate(LocalDate.now(clock).plusDays(2))
            .activityEndDate(LocalDate.now(clock).plusDays(10))
            .deadlineDate(LocalDate.now(clock).plusDays(1))
            .recruitmentType(GENERAL)
            .maxParticipants(maxParticipants)
            .currentParticipants(0)
            .description("모집 내용")
            .status(RECRUITING)
            .build());
        TeamRecruitmentChatRoom teamRoom = chatRoomRepository.save(TeamRecruitmentChatRoom.builder()
            .recruitment(recruitment)
            .roomScopeKey("TEAM")
            .roomType(TEAM)
            .status(ACTIVE)
            .build());
        chatMemberRepository.save(TeamRecruitmentChatMember.builder()
            .chatRoom(teamRoom)
            .user(author.getUser())
            .build());
        return new RecruitmentContext(recruitment, teamRoom);
    }

    private TeamRecruitmentApplication savePendingApplication(TeamRecruitment recruitment) {
        return applicationRepository.save(TeamRecruitmentApplication.builder()
            .recruitment(recruitment)
            .applicant(applicant.getUser())
            .motivation("지원 동기")
            .availability("월수금 20시 이후")
            .status(PENDING)
            .profileSnapshot("""
                {
                  "nickname": "지원자",
                  "department": "컴퓨터공학부",
                  "student_year": 2023,
                  "preferred_role": "백엔드",
                  "skills": [],
                  "activities": [],
                  "self_introduction": "소개"
                }
                """)
            .snapshotVersion(1)
            .build());
    }

    private record RecruitmentContext(
        TeamRecruitment recruitment,
        TeamRecruitmentChatRoom teamRoom
    ) {
    }
}
