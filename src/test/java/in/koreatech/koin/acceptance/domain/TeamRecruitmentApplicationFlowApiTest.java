package in.koreatech.koin.acceptance.domain;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.ACCEPTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.PENDING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.REJECTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory.PROJECT;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus.ACTIVE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus.READ_ONLY;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType.DIRECT;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType.TEAM;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType.ONLINE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.CLOSED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.RECRUITING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType.GENERAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
            .andExpect(jsonPath("$.application_id").isNumber())
            .andExpect(jsonPath("$.recruitment_id").value(recruitment.getId()))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.role").value(nullValue()))
            .andExpect(jsonPath("$.created_at").isString());

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
    void 수동_마감된_ACCEPTED_지원서는_DIRECT_CTA와_생성_조건이_모두_닫힌다() throws Exception {
        TeamRecruitment recruitment = recruitmentContext.recruitment();
        TeamRecruitmentApplication application = savePendingApplication(recruitment);

        accept(recruitment, application, authorToken)
            .andExpect(status().isNoContent());

        mockMvc.perform(put("/team-recruitments/{id}/close", recruitment.getId())
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/team-recruitments/{recruitmentId}/applications", recruitment.getId())
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.applications[0].status").value("ACCEPTED"))
            .andExpect(jsonPath("$.applications[0].can_open_direct_chat").value(false));

        mockMvc.perform(post("/chatroom/team-recruitment/{recruitmentId}/applications/{applicationId}/direct",
                recruitment.getId(), application.getId())
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("TEAM_RECRUITMENT_CLOSED"));
    }

    @Test
    void 정원충족으로_자동_마감된_ACCEPTED_지원서는_ACTIVE_TEAM_방이_있어_DIRECT를_생성할_수_있다() throws Exception {
        RecruitmentContext lastSeatContext = saveRecruitmentAndTeamRoom("한 명 모집 개인 채팅", 1);
        TeamRecruitment recruitment = lastSeatContext.recruitment();
        TeamRecruitmentApplication application = savePendingApplication(recruitment);

        accept(recruitment, application, authorToken)
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/team-recruitments/{recruitmentId}/applications", recruitment.getId())
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.applications[0].status").value("ACCEPTED"))
            .andExpect(jsonPath("$.applications[0].can_open_direct_chat").value(true));

        mockMvc.perform(post("/chatroom/team-recruitment/{recruitmentId}/applications/{applicationId}/direct",
                recruitment.getId(), application.getId())
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.chat_room_id").isNumber())
            .andExpect(jsonPath("$.room_type").value("DIRECT"))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void 마감일이_지난_RECRUITING_모집글은_스케줄러_전에도_DIRECT_CTA와_생성이_차단된다() throws Exception {
        TeamRecruitment recruitment = recruitmentContext.recruitment();
        TeamRecruitmentApplication application = savePendingApplication(recruitment);

        accept(recruitment, application, authorToken)
            .andExpect(status().isNoContent());
        expireRecruitment(recruitment);
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/team-recruitments/{recruitmentId}/applications", recruitment.getId())
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.applications[0].status").value("ACCEPTED"))
            .andExpect(jsonPath("$.applications[0].can_open_direct_chat").value(false));

        mockMvc.perform(post("/chatroom/team-recruitment/{recruitmentId}/applications/{applicationId}/direct",
                recruitment.getId(), application.getId())
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("TEAM_RECRUITMENT_CLOSED"));
    }

    @Test
    void 마감일이_지난_RECRUITING_모집글의_기존_DIRECT_방은_계속_반환된다() throws Exception {
        TeamRecruitment recruitment = recruitmentContext.recruitment();
        TeamRecruitmentApplication application = savePendingApplication(recruitment);

        accept(recruitment, application, authorToken)
            .andExpect(status().isNoContent());
        mockMvc.perform(post(
                "/chatroom/team-recruitment/{recruitmentId}/applications/{applicationId}/direct",
                recruitment.getId(), application.getId())
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.room_type").value("DIRECT"));
        entityManager.flush();
        Integer directRoomId = chatRoomRepository
            .findByRecruitment_IdAndApplication_IdAndRoomType(recruitment.getId(), application.getId(), DIRECT)
            .orElseThrow()
            .getId();
        expireRecruitment(recruitment);
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/chatroom/team-recruitment/{recruitmentId}/applications/{applicationId}/direct",
                recruitment.getId(), application.getId())
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.chat_room_id").value(directRoomId));

        mockMvc.perform(get("/chatroom/team-recruitment/{recruitmentId}/{chatRoomId}",
                recruitment.getId(), directRoomId)
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isOk());
    }

    @Test
    void 삭제된_모집글의_기존_DIRECT_방은_재요청과_조회가_가능하다() throws Exception {
        TeamRecruitment recruitment = recruitmentContext.recruitment();
        TeamRecruitmentApplication application = savePendingApplication(recruitment);

        accept(recruitment, application, authorToken)
            .andExpect(status().isNoContent());
        mockMvc.perform(post("/chatroom/team-recruitment/{recruitmentId}/applications/{applicationId}/direct",
                recruitment.getId(), application.getId())
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isCreated());

        entityManager.flush();
        entityManager.clear();
        TeamRecruitmentChatRoom directRoom = chatRoomRepository
            .findByRecruitment_IdAndApplication_IdAndRoomType(recruitment.getId(), application.getId(), DIRECT)
            .orElseThrow();

        mockMvc.perform(delete("/team-recruitments/{id}", recruitment.getId())
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isNoContent());

        assertThat(chatRoomRepository.findById(directRoom.getId()).orElseThrow().getStatus())
            .isEqualTo(READ_ONLY);

        mockMvc.perform(post("/chatroom/team-recruitment/{recruitmentId}/applications/{applicationId}/direct",
                recruitment.getId(), application.getId())
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.chat_room_id").value(directRoom.getId()))
            .andExpect(jsonPath("$.status").value("READ_ONLY"));

        mockMvc.perform(get("/chatroom/team-recruitment/{recruitmentId}/{chatRoomId}",
                recruitment.getId(), directRoom.getId())
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("READ_ONLY"));
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
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED_USER"));

        mockMvc.perform(get("/team-recruitments/{recruitmentId}/applications/{applicationId}",
                recruitmentContext.recruitment().getId(), application.getId()))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED_USER"));

        mockMvc.perform(put("/team-recruitments/{recruitmentId}/applications/{applicationId}/status",
                recruitmentContext.recruitment().getId(), application.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "status": "ACCEPTED"
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED_USER"));
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

    @Test
    void 로그인하지_않으면_지원하거나_내_지원_목록을_조회할_수_없다() throws Exception {
        mockMvc.perform(post("/team-recruitments/{recruitmentId}/applications",
                recruitmentContext.recruitment().getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "role_id": null,
                      "motivation": "지원 동기",
                      "availability": "월수금 20시 이후"
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED_USER"));

        mockMvc.perform(get("/team-recruitments/me/applications"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED_USER"));
    }

    @Test
    void 양수가_아닌_경로_ID는_잘못된_요청으로_응답한다() throws Exception {
        mockMvc.perform(post("/team-recruitments/{recruitmentId}/applications", 0)
                .header("Authorization", "Bearer " + applicantToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "role_id": null,
                      "motivation": "지원 동기",
                      "availability": "월수금 20시 이후"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("ILLEGAL_ARGUMENT"));

        mockMvc.perform(get("/team-recruitments/{recruitmentId}/applications", -1)
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("ILLEGAL_ARGUMENT"));

        mockMvc.perform(get("/team-recruitments/{recruitmentId}/applications/{applicationId}",
                recruitmentContext.recruitment().getId(), 0)
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("ILLEGAL_ARGUMENT"));

        mockMvc.perform(put("/team-recruitments/{recruitmentId}/applications/{applicationId}/status",
                recruitmentContext.recruitment().getId(), -1)
                .header("Authorization", "Bearer " + authorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "status": "ACCEPTED"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("ILLEGAL_ARGUMENT"));
    }

    @Test
    void 변환할_수_없는_경로와_query_값은_잘못된_인자로_응답한다() throws Exception {
        mockMvc.perform(get("/team-recruitments/{recruitmentId}/applications", "invalid")
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("ILLEGAL_ARGUMENT"));

        mockMvc.perform(get("/team-recruitments/me/applications")
                .queryParam("sort", "INVALID")
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("ILLEGAL_ARGUMENT"));
    }

    @Test
    void TEAM_방이_아직_없는_모집글은_지원자_목록에서_채팅_불가로_응답한다() throws Exception {
        TeamRecruitment recruitment = saveRecruitment("TEAM 방 생성 전 모집글", 2);

        mockMvc.perform(get("/team-recruitments/{recruitmentId}/applications", recruitment.getId())
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recruitment.team_chat_available").value(false))
            .andExpect(jsonPath("$.recruitment.team_chat_room_id").value(nullValue()))
            .andExpect(jsonPath("$.applications").isEmpty());
    }

    @Test
    void 작성자는_지원자_목록과_상세를_조회하고_지원을_거절할_수_있다() throws Exception {
        TeamRecruitment recruitment = recruitmentContext.recruitment();
        TeamRecruitmentApplication application = savePendingApplication(recruitment);

        mockMvc.perform(get("/team-recruitments/{recruitmentId}/applications", recruitment.getId())
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recruitment.id").value(recruitment.getId()))
            .andExpect(jsonPath("$.recruitment.team_chat_available").value(true))
            .andExpect(jsonPath("$.recruitment.team_chat_room_id").value(recruitmentContext.teamRoom().getId()))
            .andExpect(jsonPath("$.applications[0].application_id").value(application.getId()))
            .andExpect(jsonPath("$.applications[0].nickname").value("지원자"))
            .andExpect(jsonPath("$.applications[0].department").value("컴퓨터공학부"))
            .andExpect(jsonPath("$.applications[0].student_year").value(2023))
            .andExpect(jsonPath("$.applications[0].role").value(nullValue()))
            .andExpect(jsonPath("$.applications[0].status").value("PENDING"))
            .andExpect(jsonPath("$.total_count").value(1))
            .andExpect(jsonPath("$.current_count").value(1))
            .andExpect(jsonPath("$.total_page").value(1))
            .andExpect(jsonPath("$.current_page").value(1));

        mockMvc.perform(get("/team-recruitments/{recruitmentId}/applications/{applicationId}",
                recruitment.getId(), application.getId())
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.application_id").value(application.getId()))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.profile_snapshot.nickname").value("지원자"))
            .andExpect(jsonPath("$.motivation").value("지원 동기"))
            .andExpect(jsonPath("$.availability").value("월수금 20시 이후"))
            .andExpect(jsonPath("$.role").value(nullValue()))
            .andExpect(jsonPath("$.can_decide").value(true))
            .andExpect(jsonPath("$.can_open_direct_chat").value(false));

        mockMvc.perform(put("/team-recruitments/{recruitmentId}/applications/{applicationId}/status",
                recruitment.getId(), application.getId())
                .header("Authorization", "Bearer " + authorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "status": "REJECTED"
                    }
                    """))
            .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();
        assertThat(applicationRepository.findById(application.getId()).orElseThrow().getStatus())
            .isEqualTo(REJECTED);
    }

    @Test
    void GENERAL_지원에서_role_id를_생략하면_잘못된_요청으로_응답한다() throws Exception {
        mockMvc.perform(post("/team-recruitments/{recruitmentId}/applications",
                recruitmentContext.recruitment().getId())
                .header("Authorization", "Bearer " + applicantToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "motivation": "지원 동기",
                      "availability": "월수금 20시 이후"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("NOT_READABLE_HTTP_MESSAGE"));
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
        TeamRecruitment recruitment = saveRecruitment(title, maxParticipants);
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

    private TeamRecruitment saveRecruitment(String title, int maxParticipants) {
        return recruitmentRepository.save(TeamRecruitment.builder()
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
    }

    private void expireRecruitment(TeamRecruitment recruitment) {
        recruitment.modify(
            recruitment.getCategory(),
            recruitment.getTitle(),
            recruitment.getMeetingType(),
            recruitment.getActivityStartDate(),
            recruitment.getActivityEndDate(),
            LocalDate.now(clock).minusDays(1),
            recruitment.getRecruitmentType(),
            recruitment.getMaxParticipants(),
            recruitment.getDescription(),
            recruitment.getRelatedUrl(),
            recruitment.getQualification()
        );
        recruitmentRepository.save(recruitment);
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
