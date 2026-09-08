package in.koreatech.koin.acceptance.domain;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.ACCEPTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.PENDING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus.REJECTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory.PROJECT;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus.ACTIVE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomStatus.READ_ONLY;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType.TEAM;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType.ONLINE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationTargetType.MY_APPLICATIONS;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.APPLICATION_REJECTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.CLOSED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.DELETED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.RECRUITING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType.GENERAL;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType.ROLE_BASED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMember;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentNotification;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentProfile;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentRole;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentApplicationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMemberRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentNotificationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentProfileRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRoleRepository;

/**
 * 모집글 마감/삭제 후속 처리와 역할 수정 시 DB 제약을 실제 DB 로 검증한다.
 * 단위 테스트는 EntityManager 를 mock 하므로 unique/FK 위반을 잡지 못한다.
 */
class TeamRecruitmentArticleFlowApiTest extends AcceptanceTest {

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    @Autowired
    private TeamRecruitmentRepository recruitmentRepository;

    @Autowired
    private TeamRecruitmentRoleRepository roleRepository;

    @Autowired
    private TeamRecruitmentProfileRepository profileRepository;

    @Autowired
    private TeamRecruitmentApplicationRepository applicationRepository;

    @Autowired
    private TeamRecruitmentChatRoomRepository chatRoomRepository;

    @Autowired
    private TeamRecruitmentChatMemberRepository chatMemberRepository;

    @Autowired
    private TeamRecruitmentNotificationRepository notificationRepository;

    private static final String PROFILE_SNAPSHOT = """
        {
          "nickname": "지원자",
          "department": "컴퓨터공학부",
          "student_year": 2023,
          "preferred_role": "백엔드",
          "skills": [],
          "activities": [],
          "self_introduction": "소개"
        }
        """;

    private Student author;
    private Student applicant;
    private String authorToken;
    private String applicantToken;

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
    }

    @Test
    @DisplayName("수동 마감하면 대기 지원서가 거절되고 채팅방이 READ_ONLY 로 바뀌며 알림이 남는다")
    void 수동_마감_후속_처리() throws Exception {
        TeamRecruitment recruitment = saveGeneralRecruitment("수동 마감", 3, 0);
        TeamRecruitmentChatRoom teamRoom = saveTeamRoom(recruitment);
        TeamRecruitmentApplication application = saveApplication(recruitment, null, PENDING);

        mockMvc.perform(put("/team-recruitments/{id}/close", recruitment.getId())
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isNoContent());

        assertThat(recruitmentRepository.findById(recruitment.getId()).orElseThrow().getStatus())
            .isEqualTo(CLOSED);
        assertThat(applicationRepository.findById(application.getId()).orElseThrow().getStatus())
            .isEqualTo(REJECTED);
        assertThat(chatRoomRepository.findById(teamRoom.getId()).orElseThrow().getStatus())
            .isEqualTo(READ_ONLY);
        assertThat(notificationRepository.findAllByRecipient_IdAndIsDeletedFalse(applicant.getUser().getId()))
            .extracting(TeamRecruitmentNotification::getType)
            .contains(APPLICATION_REJECTED);
    }

    @Test
    @DisplayName("삭제하면 대기 지원서가 취소되고 삭제 문구로 알림이 남는다")
    void 삭제_후속_처리() throws Exception {
        TeamRecruitment recruitment = saveGeneralRecruitment("삭제", 3, 0);
        TeamRecruitmentChatRoom teamRoom = saveTeamRoom(recruitment);
        TeamRecruitmentApplication application = saveApplication(recruitment, null, PENDING);

        mockMvc.perform(delete("/team-recruitments/{id}", recruitment.getId())
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isNoContent());

        assertThat(recruitmentRepository.findById(recruitment.getId()).orElseThrow().getStatus())
            .isEqualTo(DELETED);
        assertThat(applicationRepository.findById(application.getId()).orElseThrow().getDecisionReason())
            .isEqualTo("RECRUITMENT_DELETED");
        assertThat(chatRoomRepository.findById(teamRoom.getId()).orElseThrow().getStatus())
            .isEqualTo(READ_ONLY);
        assertThat(notificationRepository.findAllByRecipient_IdAndIsDeletedFalse(applicant.getUser().getId()))
            .extracting(TeamRecruitmentNotification::getMessagePreview)
            .anyMatch(message -> message.contains("삭제되어"));
    }

    @Test
    @DisplayName("알림 단건 읽음 처리는 수신자와 미삭제 최초 읽음만 변경하고 반복 호출에도 읽은 시각을 보존한다")
    void 알림_단건_읽음_처리_멱등성() throws Exception {
        TeamRecruitment recruitment = saveGeneralRecruitment("알림 읽음", 3, 0);
        TeamRecruitmentNotification unread = saveNotification(recruitment, applicant.getUser(), null, false);
        TeamRecruitmentNotification deletedUnread = saveNotification(recruitment, applicant.getUser(), null, true);
        TeamRecruitmentNotification otherUserUnread = saveNotification(recruitment, author.getUser(), null, false);
        entityManager.flush();

        mockMvc.perform(post("/team-recruitments/notifications/{notificationId}/read", unread.getId())
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isNoContent());

        entityManager.clear();
        LocalDateTime firstReadAt = notificationRepository.findById(unread.getId()).orElseThrow().getReadAt();
        assertThat(firstReadAt).isNotNull();

        mockMvc.perform(post("/team-recruitments/notifications/{notificationId}/read", unread.getId())
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isNoContent());
        mockMvc.perform(post("/team-recruitments/notifications/{notificationId}/read", deletedUnread.getId())
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isNoContent());
        mockMvc.perform(post("/team-recruitments/notifications/{notificationId}/read", otherUserUnread.getId())
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isNoContent());
        mockMvc.perform(post("/team-recruitments/notifications/{notificationId}/read", 999999)
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isNoContent());

        entityManager.clear();
        assertThat(notificationRepository.findById(unread.getId()).orElseThrow().getReadAt())
            .isEqualTo(firstReadAt);
        assertThat(notificationRepository.findById(deletedUnread.getId()).orElseThrow().getReadAt())
            .isNull();
        assertThat(notificationRepository.findById(otherUserUnread.getId()).orElseThrow().getReadAt())
            .isNull();
        assertThat(notificationRepository.countByRecipient_IdAndIsDeletedFalse(applicant.getUser().getId()))
            .isEqualTo(1L);
        mockMvc.perform(get("/team-recruitments/notifications")
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total_count").value(1))
            .andExpect(jsonPath("$.unread_count").value(0))
            .andExpect(jsonPath("$.notifications.length()").value(1))
            .andExpect(jsonPath("$.notifications[0].id").value(unread.getId()));
    }

    @Test
    @DisplayName("알림 단건 삭제는 수신자와 미삭제 알림만 변경하고 반복·대상 없음에도 204를 반환한다")
    void 알림_단건_삭제_멱등성() throws Exception {
        TeamRecruitment recruitment = saveGeneralRecruitment("알림 삭제", 3, 0);
        TeamRecruitmentNotification unread = saveNotification(recruitment, applicant.getUser(), null, false);
        TeamRecruitmentNotification deleted = saveNotification(recruitment, applicant.getUser(), null, true);
        TeamRecruitmentNotification otherUser = saveNotification(recruitment, author.getUser(), null, false);
        entityManager.flush();

        mockMvc.perform(delete("/team-recruitments/notifications/{notificationId}", unread.getId())
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isNoContent());
        mockMvc.perform(delete("/team-recruitments/notifications/{notificationId}", unread.getId())
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isNoContent());
        mockMvc.perform(delete("/team-recruitments/notifications/{notificationId}", deleted.getId())
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isNoContent());
        mockMvc.perform(delete("/team-recruitments/notifications/{notificationId}", otherUser.getId())
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isNoContent());
        mockMvc.perform(delete("/team-recruitments/notifications/{notificationId}", 999999)
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isNoContent());

        entityManager.clear();
        assertThat(notificationRepository.findById(unread.getId()).orElseThrow().getIsDeleted())
            .isTrue();
        assertThat(notificationRepository.findById(deleted.getId()).orElseThrow().getIsDeleted())
            .isTrue();
        assertThat(notificationRepository.findById(otherUser.getId()).orElseThrow().getIsDeleted())
            .isFalse();
        assertThat(notificationRepository.countByRecipient_IdAndIsDeletedFalse(applicant.getUser().getId()))
            .isZero();
        mockMvc.perform(get("/team-recruitments/notifications")
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total_count").value(0))
            .andExpect(jsonPath("$.unread_count").value(0))
            .andExpect(jsonPath("$.notifications.length()").value(0));
    }

    @Test
    @DisplayName("정원을 승인 인원과 같게 줄이면 마감되지만 TEAM 채팅방은 ACTIVE 를 유지한다")
    void 정원_충족_자동_마감() throws Exception {
        TeamRecruitment recruitment = saveGeneralRecruitment("정원 충족", 3, 1);
        TeamRecruitmentChatRoom teamRoom = saveTeamRoom(recruitment);
        saveApplication(recruitment, null, ACCEPTED);

        mockMvc.perform(put("/team-recruitments/{id}", recruitment.getId())
                .header("Authorization", "Bearer " + authorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(generalBody(1)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CLOSED"));

        assertThat(chatRoomRepository.findById(teamRoom.getId()).orElseThrow().getStatus())
            .isEqualTo(ACTIVE);
    }

    @Test
    @DisplayName("두 역할의 이름을 서로 맞바꿔도 unique 제약을 위반하지 않는다")
    void 역할_이름_교환() throws Exception {
        TeamRecruitment recruitment = saveRoleBasedRecruitment();
        List<TeamRecruitmentRole> roles =
            roleRepository.findAllByRecruitment_IdOrderByDisplayOrderAsc(recruitment.getId());
        Integer first = roles.get(0).getId();
        Integer second = roles.get(1).getId();

        mockMvc.perform(put("/team-recruitments/{id}", recruitment.getId())
                .header("Authorization", "Bearer " + authorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(roleBasedBody("""
                    {"id": %d, "name": "Backend", "max_participants": 1},
                    {"id": %d, "name": "PM", "max_participants": 2}
                    """.formatted(first, second))))
            .andExpect(status().isOk());

        assertThat(roleRepository.findAllByRecruitment_IdOrderByDisplayOrderAsc(recruitment.getId()))
            .extracting(TeamRecruitmentRole::getId, TeamRecruitmentRole::getName)
            .containsExactly(
                tuple(first, "Backend"),
                tuple(second, "PM"));
    }

    @Test
    @DisplayName("수정 시 역할 객체의 name 키가 max_participants 앞에서 중복되면 400 이고 기존 값은 유지된다")
    void duplicateRoleNameKeyBeforeMaxParticipantsOnUpdate() throws Exception {
        TeamRecruitment recruitment = saveRoleBasedRecruitment();
        entityManager.flush();
        String titleBefore = recruitment.getTitle();
        List<RoleSnapshot> rolesBefore = roleSnapshots(recruitment.getId());
        Integer roleId = rolesBefore.get(0).id();
        RoleSnapshot retainedRole = rolesBefore.get(1);

        assertNotReadableHttpMessage(updateRoleBasedRecruitment(recruitment, """
            {"id": %d, "name": "PM", "name": "PM2", "max_participants": 1},
            {"id": %d, "name": "%s", "max_participants": %d}
            """.formatted(
            roleId,
            retainedRole.id(),
            retainedRole.name(),
            retainedRole.maxParticipants())));

        assertRecruitmentUnchanged(recruitment.getId(), titleBefore, rolesBefore);
    }

    @Test
    @DisplayName("수정 시 역할 객체의 name 키가 max_participants 뒤에서 중복되면 400 이고 기존 값은 유지된다")
    void duplicateRoleNameKeyAfterMaxParticipantsOnUpdate() throws Exception {
        TeamRecruitment recruitment = saveRoleBasedRecruitment();
        entityManager.flush();
        String titleBefore = recruitment.getTitle();
        List<RoleSnapshot> rolesBefore = roleSnapshots(recruitment.getId());
        Integer roleId = rolesBefore.get(0).id();
        RoleSnapshot retainedRole = rolesBefore.get(1);

        assertNotReadableHttpMessage(updateRoleBasedRecruitment(recruitment, """
            {"id": %d, "name": "PM", "max_participants": 1, "name": "PM2"},
            {"id": %d, "name": "%s", "max_participants": %d}
            """.formatted(
            roleId,
            retainedRole.id(),
            retainedRole.name(),
            retainedRole.maxParticipants())));

        assertRecruitmentUnchanged(recruitment.getId(), titleBefore, rolesBefore);
    }

    @Test
    @DisplayName("뒤에 자리가 없으면 기존 역할을 앞으로 당기고 새 역할을 마지막에 붙인다")
    void 역할_표시_순서_압축() throws Exception {
        TeamRecruitment recruitment = saveRecruitmentWithRoleOrders(2, 3, 4, 5);
        List<TeamRecruitmentRole> roles =
            roleRepository.findAllByRecruitment_IdOrderByDisplayOrderAsc(recruitment.getId());

        mockMvc.perform(put("/team-recruitments/{id}", recruitment.getId())
                .header("Authorization", "Bearer " + authorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(roleBasedBody("""
                    {"id": %d, "name": "R2", "max_participants": 1},
                    {"id": %d, "name": "R3", "max_participants": 1},
                    {"id": %d, "name": "R4", "max_participants": 1},
                    {"id": %d, "name": "R5", "max_participants": 1},
                    {"name": "R6", "max_participants": 1}
                    """.formatted(
                    roles.get(0).getId(), roles.get(1).getId(),
                    roles.get(2).getId(), roles.get(3).getId()))))
            .andExpect(status().isOk());

        assertThat(roleRepository.findAllByRecruitment_IdOrderByDisplayOrderAsc(recruitment.getId()))
            .extracting(TeamRecruitmentRole::getName, TeamRecruitmentRole::getDisplayOrder)
            .containsExactly(
                tuple("R2", 1),
                tuple("R3", 2),
                tuple("R4", 3),
                tuple("R5", 4),
                tuple("R6", 5));
    }

    @Test
    @DisplayName("마지막 순서 역할만 남은 상태에서 역할을 추가하면 새 역할이 뒤에 온다")
    void 마지막_순서_역할만_남은_상태에서_역할_추가() throws Exception {
        TeamRecruitment recruitment = saveRecruitmentWithRoleOrders(5);
        Integer roleId = roleRepository
            .findAllByRecruitment_IdOrderByDisplayOrderAsc(recruitment.getId()).get(0).getId();

        mockMvc.perform(put("/team-recruitments/{id}", recruitment.getId())
                .header("Authorization", "Bearer " + authorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(roleBasedBody("""
                    {"id": %d, "name": "R5", "max_participants": 1},
                    {"name": "R6", "max_participants": 1}
                    """.formatted(roleId))))
            .andExpect(status().isOk());

        assertThat(roleRepository.findAllByRecruitment_IdOrderByDisplayOrderAsc(recruitment.getId()))
            .extracting(TeamRecruitmentRole::getName, TeamRecruitmentRole::getDisplayOrder)
            .containsExactly(
                tuple("R5", 1),
                tuple("R6", 2));
    }

    @Test
    @DisplayName("거절된 지원서만 남은 역할도 삭제할 수 없다")
    void 거절된_지원서가_있는_역할_삭제_차단() throws Exception {
        TeamRecruitment recruitment = saveRoleBasedRecruitment();
        List<TeamRecruitmentRole> roles =
            roleRepository.findAllByRecruitment_IdOrderByDisplayOrderAsc(recruitment.getId());
        TeamRecruitmentRole target = roles.get(0);
        saveApplication(recruitment, target, REJECTED);

        mockMvc.perform(put("/team-recruitments/{id}", recruitment.getId())
                .header("Authorization", "Bearer " + authorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(roleBasedBody("""
                    {"id": %d, "name": "PM", "max_participants": 2}
                    """.formatted(roles.get(1).getId()))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("TEAM_RECRUITMENT_ROLE_UPDATE_NOT_ALLOWED"));

        assertThat(roleRepository.findAllByRecruitment_IdOrderByDisplayOrderAsc(recruitment.getId()))
            .hasSize(2);
    }

    @Test
    @DisplayName("마감된 모집글은 정원이 남은 역할도 is_closed 가 true 이다")
    void 마감된_모집글의_역할은_닫힌다() throws Exception {
        TeamRecruitment recruitment = saveRoleBasedRecruitment();

        mockMvc.perform(put("/team-recruitments/{id}/close", recruitment.getId())
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/team-recruitments/{id}", recruitment.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CLOSED"))
            .andExpect(jsonPath("$.roles[0].is_closed").value(true))
            .andExpect(jsonPath("$.roles[1].is_closed").value(true));
    }

    private TeamRecruitment saveGeneralRecruitment(String title, int maxParticipants, int currentParticipants) {
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
            .currentParticipants(currentParticipants)
            .description("모집 내용")
            .status(RECRUITING)
            .build());
    }

    private TeamRecruitment saveRoleBasedRecruitment() {
        TeamRecruitment recruitment = TeamRecruitment.builder()
            .author(author.getUser())
            .category(PROJECT)
            .title("역할 모집")
            .meetingType(ONLINE)
            .activityStartDate(LocalDate.now(clock).plusDays(2))
            .activityEndDate(LocalDate.now(clock).plusDays(10))
            .deadlineDate(LocalDate.now(clock).plusDays(1))
            .recruitmentType(ROLE_BASED)
            .maxParticipants(3)
            .currentParticipants(0)
            .description("모집 내용")
            .status(RECRUITING)
            .build();
        recruitment.addRole(TeamRecruitmentRole.builder()
            .name("PM").maxParticipants(1).currentParticipants(0).displayOrder(1).build());
        recruitment.addRole(TeamRecruitmentRole.builder()
            .name("Backend").maxParticipants(2).currentParticipants(0).displayOrder(2).build());
        return recruitmentRepository.save(recruitment);
    }

    private TeamRecruitmentChatRoom saveTeamRoom(TeamRecruitment recruitment) {
        TeamRecruitmentChatRoom teamRoom =
            chatRoomRepository.save(TeamRecruitmentChatRoom.createTeamRoom(recruitment));
        chatMemberRepository.save(TeamRecruitmentChatMember.builder()
            .chatRoom(teamRoom)
            .user(author.getUser())
            .build());
        return teamRoom;
    }

    private TeamRecruitmentApplication saveApplication(
        TeamRecruitment recruitment,
        TeamRecruitmentRole role,
        TeamRecruitmentApplicationStatus status
    ) {
        return applicationRepository.save(TeamRecruitmentApplication.builder()
            .recruitment(recruitment)
            .applicant(applicant.getUser())
            .role(role)
            .motivation("지원 동기")
            .availability("월수금")
            .status(status)
            .profileSnapshot(PROFILE_SNAPSHOT)
            .snapshotVersion(1)
            .build());
    }

    private TeamRecruitmentNotification saveNotification(
        TeamRecruitment recruitment,
        User recipient,
        LocalDateTime readAt,
        boolean isDeleted
    ) {
        return notificationRepository.save(TeamRecruitmentNotification.builder()
            .recipient(recipient)
            .type(APPLICATION_REJECTED)
            .targetType(MY_APPLICATIONS)
            .messagePreview("알림")
            .recruitment(recruitment)
            .readAt(readAt)
            .isDeleted(isDeleted)
            .build());
    }

    private String generalBody(int maxParticipants) {
        return """
            {
              "category": "PROJECT",
              "title": "수정된 제목",
              "meeting_type": "ONLINE",
              "activity_start_date": "%s",
              "activity_end_date": "%s",
              "deadline_date": "%s",
              "recruitment_type": "GENERAL",
              "max_participants": %d,
              "roles": [],
              "description": "수정한 내용",
              "related_url": null,
              "qualification": null
            }
            """.formatted(
            LocalDate.now(clock).plusDays(2),
            LocalDate.now(clock).plusDays(10),
            LocalDate.now(clock).plusDays(1),
            maxParticipants);
    }

    private TeamRecruitment saveRecruitmentWithRoleOrders(int... displayOrders) {
        TeamRecruitment recruitment = TeamRecruitment.builder()
            .author(author.getUser())
            .category(PROJECT)
            .title("역할 모집")
            .meetingType(ONLINE)
            .activityStartDate(LocalDate.now(clock).plusDays(2))
            .activityEndDate(LocalDate.now(clock).plusDays(10))
            .deadlineDate(LocalDate.now(clock).plusDays(1))
            .recruitmentType(ROLE_BASED)
            .maxParticipants(displayOrders.length)
            .currentParticipants(0)
            .description("모집 내용")
            .status(RECRUITING)
            .build();
        for (int displayOrder : displayOrders) {
            recruitment.addRole(TeamRecruitmentRole.builder()
                .name("R" + displayOrder).maxParticipants(1).currentParticipants(0)
                .displayOrder(displayOrder).build());
        }
        return recruitmentRepository.save(recruitment);
    }

    private String roleBasedBody(String roles) {
        return """
            {
              "category": "PROJECT",
              "title": "수정된 제목",
              "meeting_type": "ONLINE",
              "activity_start_date": "%s",
              "activity_end_date": "%s",
              "deadline_date": "%s",
              "recruitment_type": "ROLE_BASED",
              "roles": [%s],
              "description": "수정한 내용",
              "related_url": null,
              "qualification": null
            }
            """.formatted(
            LocalDate.now(clock).plusDays(2),
            LocalDate.now(clock).plusDays(10),
            LocalDate.now(clock).plusDays(1),
            roles);
    }

    private ResultActions updateRoleBasedRecruitment(TeamRecruitment recruitment, String roles) throws Exception {
        return mockMvc.perform(put("/team-recruitments/{id}", recruitment.getId())
            .header("Authorization", "Bearer " + authorToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(roleBasedBody(roles)));
    }

    private void assertNotReadableHttpMessage(ResultActions result) throws Exception {
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("NOT_READABLE_HTTP_MESSAGE"));
    }

    private List<RoleSnapshot> roleSnapshots(Integer recruitmentId) {
        return roleRepository.findAllByRecruitment_IdOrderByDisplayOrderAsc(recruitmentId).stream()
            .map(role -> new RoleSnapshot(
                role.getId(),
                role.getName(),
                role.getMaxParticipants(),
                role.getCurrentParticipants(),
                role.getDisplayOrder()))
            .toList();
    }

    private void assertRecruitmentUnchanged(
        Integer recruitmentId,
        String titleBefore,
        List<RoleSnapshot> rolesBefore
    ) {
        entityManager.flush();
        entityManager.clear();

        assertThat(recruitmentRepository.findById(recruitmentId).orElseThrow().getTitle())
            .isEqualTo(titleBefore);
        assertThat(roleSnapshots(recruitmentId)).containsExactlyElementsOf(rolesBefore);
    }

    private record RoleSnapshot(
        Integer id,
        String name,
        Integer maxParticipants,
        Integer currentParticipants,
        Integer displayOrder
    ) {
    }
}
