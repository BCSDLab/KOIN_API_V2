package in.koreatech.koin.acceptance.domain;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory.PROJECT;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType.ONLINE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationTargetType.MY_APPLICATIONS;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType.APPLICATION_REJECTED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.RECRUITING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType.GENERAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentNotification;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentNotificationRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.user.model.User;

class TeamRecruitmentNotificationApiTest extends AcceptanceTest {

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    @Autowired
    private TeamRecruitmentRepository recruitmentRepository;

    @Autowired
    private TeamRecruitmentNotificationRepository notificationRepository;

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
    }

    @Test
    @DisplayName("팀원 모집 알림 목록은 사용자별로 격리되고 페이지·전체 읽음·전체 삭제가 HTTP로 동작한다")
    void notificationListIsolationPaginationAndBulkActions() throws Exception {
        TeamRecruitment recruitment = saveRecruitment("알림 목록");
        TeamRecruitmentNotification first = saveNotification(recruitment, applicant.getUser(), "첫 알림");
        TeamRecruitmentNotification second = saveNotification(recruitment, applicant.getUser(), "둘째 알림");
        TeamRecruitmentNotification third = saveNotification(recruitment, applicant.getUser(), "셋째 알림");
        TeamRecruitmentNotification authorNotification = saveNotification(recruitment, author.getUser(), "작성자 알림");
        entityManager.flush();

        List<Integer> applicantNotificationIds = List.of(first.getId(), second.getId(), third.getId()).stream()
            .sorted(Comparator.reverseOrder())
            .toList();

        mockMvc.perform(get("/team-recruitments/notifications")
                .param("page", "1")
                .param("limit", "2")
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total_count").value(3))
            .andExpect(jsonPath("$.current_count").value(2))
            .andExpect(jsonPath("$.total_page").value(2))
            .andExpect(jsonPath("$.current_page").value(1))
            .andExpect(jsonPath("$.unread_count").value(3))
            .andExpect(jsonPath("$.notifications.length()").value(2))
            .andExpect(jsonPath("$.notifications[0].id").value(applicantNotificationIds.get(0)))
            .andExpect(jsonPath("$.notifications[1].id").value(applicantNotificationIds.get(1)));

        mockMvc.perform(get("/team-recruitments/notifications")
                .param("page", "2")
                .param("limit", "2")
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total_count").value(3))
            .andExpect(jsonPath("$.current_count").value(1))
            .andExpect(jsonPath("$.total_page").value(2))
            .andExpect(jsonPath("$.current_page").value(2))
            .andExpect(jsonPath("$.unread_count").value(3))
            .andExpect(jsonPath("$.notifications[0].id").value(applicantNotificationIds.get(2)));

        mockMvc.perform(get("/team-recruitments/notifications")
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total_count").value(1))
            .andExpect(jsonPath("$.unread_count").value(1))
            .andExpect(jsonPath("$.notifications.length()").value(1))
            .andExpect(jsonPath("$.notifications[0].id").value(authorNotification.getId()))
            .andExpect(jsonPath("$.notifications[0].message_preview").value("작성자 알림"));

        mockMvc.perform(post("/team-recruitments/notifications/mark-all-read")
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isNoContent());
        entityManager.flush();
        entityManager.clear();
        List<LocalDateTime> firstReadAt = applicantNotificationIds.stream()
            .map(id -> notificationRepository.findById(id).orElseThrow().getReadAt())
            .toList();
        assertThat(firstReadAt).doesNotContainNull();

        mockMvc.perform(post("/team-recruitments/notifications/mark-all-read")
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isNoContent());
        entityManager.flush();
        entityManager.clear();
        assertThat(applicantNotificationIds.stream()
            .map(id -> notificationRepository.findById(id).orElseThrow().getReadAt())
            .toList()).containsExactlyElementsOf(firstReadAt);
        assertThat(notificationRepository.findById(authorNotification.getId()).orElseThrow().getReadAt()).isNull();

        mockMvc.perform(get("/team-recruitments/notifications")
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.unread_count").value(0))
            .andExpect(jsonPath("$.notifications[*].is_read").value(everyItem(equalTo(true))));

        mockMvc.perform(delete("/team-recruitments/notifications")
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isNoContent());
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(delete("/team-recruitments/notifications")
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isNoContent());
        entityManager.flush();
        entityManager.clear();
        assertThat(applicantNotificationIds)
            .allSatisfy(id -> assertThat(notificationRepository.findById(id).orElseThrow().getIsDeleted()).isTrue());
        assertThat(notificationRepository.findById(authorNotification.getId()).orElseThrow().getIsDeleted()).isFalse();

        mockMvc.perform(get("/team-recruitments/notifications")
                .header("Authorization", "Bearer " + applicantToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total_count").value(0))
            .andExpect(jsonPath("$.unread_count").value(0))
            .andExpect(jsonPath("$.notifications").isEmpty());

        mockMvc.perform(get("/team-recruitments/notifications")
                .header("Authorization", "Bearer " + authorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total_count").value(1))
            .andExpect(jsonPath("$.unread_count").value(1))
            .andExpect(jsonPath("$.notifications[0].id").value(authorNotification.getId()))
            .andExpect(jsonPath("$.notifications[0].is_read").value(false));
    }

    private TeamRecruitment saveRecruitment(String title) {
        return recruitmentRepository.save(TeamRecruitment.builder()
            .author(author.getUser())
            .category(PROJECT)
            .title(title)
            .meetingType(ONLINE)
            .activityStartDate(LocalDate.now(clock).plusDays(2))
            .activityEndDate(LocalDate.now(clock).plusDays(10))
            .deadlineDate(LocalDate.now(clock).plusDays(1))
            .recruitmentType(GENERAL)
            .maxParticipants(3)
            .currentParticipants(0)
            .description("모집 내용")
            .status(RECRUITING)
            .build());
    }

    private TeamRecruitmentNotification saveNotification(
        TeamRecruitment recruitment,
        User recipient,
        String messagePreview
    ) {
        return notificationRepository.save(TeamRecruitmentNotification.builder()
            .recipient(recipient)
            .type(APPLICATION_REJECTED)
            .targetType(MY_APPLICATIONS)
            .messagePreview(messagePreview)
            .recruitment(recruitment)
            .build());
    }
}
