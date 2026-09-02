package in.koreatech.koin.acceptance.domain;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory.PROJECT;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType.ONLINE;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.CLOSED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.RECRUITING;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType.GENERAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatMemberRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentChatRoomRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentRoleRepository;

/**
 * 담당 API 의 요청/응답 계약과 인증, 권한을 확인한다.
 */
class TeamRecruitmentArticleContractApiTest extends AcceptanceTest {

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    @Autowired
    private TeamRecruitmentRepository recruitmentRepository;

    @Autowired
    private TeamRecruitmentRoleRepository roleRepository;

    @Autowired
    private TeamRecruitmentChatRoomRepository chatRoomRepository;

    @Autowired
    private TeamRecruitmentChatMemberRepository chatMemberRepository;

    private Student author;
    private Student otherStudent;
    private String authorToken;
    private String otherToken;

    @BeforeEach
    void setUp() {
        clear();
        Department department = departmentFixture.컴퓨터공학부();
        author = userFixture.준호_학생(department, null);
        otherStudent = userFixture.성빈_학생(department);
        authorToken = userFixture.getToken(author.getUser());
        otherToken = userFixture.getToken(otherStudent.getUser());
    }

    private String roleBasedBody(String roles) {
        return """
            {
              "category": "CONTEST",
              "title": "AI 아이디어 공모전 팀원 모집",
              "meeting_type": "ONLINE",
              "activity_start_date": "%s",
              "activity_end_date": "%s",
              "deadline_date": "%s",
              "recruitment_type": "ROLE_BASED",
              "roles": [%s],
              "description": "공모전 팀원을 모집합니다.",
              "related_url": "https://example.com",
              "qualification": "기획 경험이 있는 분"
            }
            """.formatted(
            LocalDate.now(clock).plusDays(10),
            LocalDate.now(clock).plusDays(20),
            LocalDate.now(clock).plusDays(3),
            roles);
    }

    private ResultActions createRoleBasedRecruitment(String roles) throws Exception {
        return mockMvc.perform(post("/team-recruitments")
            .header("Authorization", "Bearer " + authorToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(roleBasedBody(roles)));
    }

    private void assertNotReadableHttpMessage(ResultActions result) throws Exception {
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("NOT_READABLE_HTTP_MESSAGE"));
    }

    private List<Long> recruitmentGraphRowCounts() {
        return List.of(
            entityManager.createQuery("select count(recruitment) from TeamRecruitment recruitment", Long.class)
                .getSingleResult(),
            entityManager.createQuery("select count(role) from TeamRecruitmentRole role", Long.class)
                .getSingleResult(),
            entityManager.createQuery("select count(chatRoom) from TeamRecruitmentChatRoom chatRoom", Long.class)
                .getSingleResult(),
            entityManager.createQuery("select count(chatMember) from TeamRecruitmentChatMember chatMember", Long.class)
                .getSingleResult());
    }

    private void assertRecruitmentGraphUnchanged(List<Long> before) {
        entityManager.flush();
        entityManager.clear();
        assertThat(recruitmentGraphRowCounts()).containsExactlyElementsOf(before);
    }

    private String generalBody(int maxParticipants) {
        return """
            {
              "category": "STUDY",
              "title": "알고리즘 스터디",
              "meeting_type": "ONLINE",
              "activity_start_date": "%s",
              "activity_end_date": "%s",
              "deadline_date": "%s",
              "recruitment_type": "GENERAL",
              "max_participants": %d,
              "roles": [],
              "description": "스터디원을 모집합니다.",
              "related_url": null,
              "qualification": null
            }
            """.formatted(
            LocalDate.now(clock).plusDays(10),
            LocalDate.now(clock).plusDays(20),
            LocalDate.now(clock).plusDays(3),
            maxParticipants);
    }

    private TeamRecruitment saveRecruitment(Student writer, String title) {
        return recruitmentRepository.save(TeamRecruitment.builder()
            .author(writer.getUser())
            .category(PROJECT)
            .title(title)
            .meetingType(ONLINE)
            .activityStartDate(LocalDate.now(clock).plusDays(10))
            .activityEndDate(LocalDate.now(clock).plusDays(20))
            .deadlineDate(LocalDate.now(clock).plusDays(3))
            .recruitmentType(GENERAL)
            .maxParticipants(5)
            .currentParticipants(0)
            .description("모집 내용")
            .status(RECRUITING)
            .build());
    }

    @Nested
    @DisplayName("POST /team-recruitments")
    class CreateRecruitment {

        @Test
        @DisplayName("ROLE_BASED 모집글과 TEAM 채팅방, 작성자 멤버가 함께 생성된다")
        void createsRoleBasedRecruitmentWithTeamRoom() throws Exception {
            mockMvc.perform(post("/team-recruitments")
                    .header("Authorization", "Bearer " + authorToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(roleBasedBody("""
                        {"name": "PM", "max_participants": 1},
                        {"name": "Backend", "max_participants": 2}
                        """)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

            TeamRecruitment saved = recruitmentRepository
                .findAllByAuthor_Id(author.getUser().getId(), org.springframework.data.domain.Pageable.unpaged())
                .getContent().get(0);
            assertThat(saved.getMaxParticipants()).isEqualTo(3);
            assertThat(roleRepository.findAllByRecruitment_IdOrderByDisplayOrderAsc(saved.getId()))
                .extracting(role -> role.getDisplayOrder())
                .containsExactly(1, 2);
            assertThat(chatRoomRepository.findAllByRecruitment_Id(saved.getId())).hasSize(1);
            Integer teamRoomId = chatRoomRepository.findAllByRecruitment_Id(saved.getId()).get(0).getId();
            assertThat(chatMemberRepository.existsByChatRoom_IdAndUser_Id(teamRoomId, author.getUser().getId()))
                .isTrue();
        }

        @Test
        @DisplayName("GENERAL 모집글을 생성한다")
        void createsGeneralRecruitment() throws Exception {
            mockMvc.perform(post("/team-recruitments")
                    .header("Authorization", "Bearer " + authorToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(generalBody(5)))
                .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("미인증 요청은 401 이다")
        void unauthenticated() throws Exception {
            mockMvc.perform(post("/team-recruitments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(generalBody(5)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("역할 정원의 합이 10을 넘으면 400 이다")
        void totalCapacityOverLimit() throws Exception {
            mockMvc.perform(post("/team-recruitments")
                    .header("Authorization", "Bearer " + authorToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(roleBasedBody("""
                        {"name": "Backend", "max_participants": 6},
                        {"name": "Frontend", "max_participants": 6}
                        """)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));
        }

        @Test
        @DisplayName("역할명이 대소문자만 달라도 400 이다")
        void duplicateRoleName() throws Exception {
            mockMvc.perform(post("/team-recruitments")
                    .header("Authorization", "Bearer " + authorToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(roleBasedBody("""
                        {"name": "PM", "max_participants": 1},
                        {"name": "pm", "max_participants": 1}
                        """)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));
        }

        @Test
        @DisplayName("역할 객체의 name 키가 max_participants 앞에서 중복되면 400 이다")
        void duplicateRoleNameKeyBeforeMaxParticipantsOnCreate() throws Exception {
            List<Long> before = recruitmentGraphRowCounts();

            assertNotReadableHttpMessage(createRoleBasedRecruitment("""
                {"name": "Backend", "name": "Backend2", "max_participants": 1}
                """));

            assertRecruitmentGraphUnchanged(before);
        }

        @Test
        @DisplayName("역할 객체의 name 키가 max_participants 뒤에서 중복되면 400 이다")
        void duplicateRoleNameKeyAfterMaxParticipantsOnCreate() throws Exception {
            List<Long> before = recruitmentGraphRowCounts();

            assertNotReadableHttpMessage(createRoleBasedRecruitment("""
                {"name": "Backend", "max_participants": 1, "name": "Backend2"}
                """));

            assertRecruitmentGraphUnchanged(before);
        }

        @Test
        @DisplayName("지원 마감일이 활동 시작일보다 이후면 400 이다")
        void deadlineAfterActivityStart() throws Exception {
            String body = """
                {
                  "category": "STUDY",
                  "title": "잘못된 기간",
                  "meeting_type": "ONLINE",
                  "activity_start_date": "%s",
                  "activity_end_date": "%s",
                  "deadline_date": "%s",
                  "recruitment_type": "GENERAL",
                  "max_participants": 3,
                  "roles": [],
                  "description": "설명",
                  "related_url": null,
                  "qualification": null
                }
                """.formatted(
                LocalDate.now(clock).plusDays(10),
                LocalDate.now(clock).plusDays(20),
                LocalDate.now(clock).plusDays(11));

            mockMvc.perform(post("/team-recruitments")
                    .header("Authorization", "Bearer " + authorToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TEAM_RECRUITMENT_INVALID_DEADLINE_DATE"));
        }
    }

    @Nested
    @DisplayName("GET /team-recruitments")
    class GetRecruitments {

        @Test
        @DisplayName("비로그인으로 목록을 조회하고 페이지네이션 필드를 받는다")
        void listsWithoutLogin() throws Exception {
            saveRecruitment(author, "첫 번째");
            saveRecruitment(author, "두 번째");

            mockMvc.perform(get("/team-recruitments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_count").value(2))
                .andExpect(jsonPath("$.current_count").value(2))
                .andExpect(jsonPath("$.total_page").value(1))
                .andExpect(jsonPath("$.current_page").value(1))
                .andExpect(jsonPath("$.recruitments[0].d_day").exists())
                .andExpect(jsonPath("$.recruitments[0].status").value("RECRUITING"));
        }

        @Test
        @DisplayName("limit 은 50 을 넘지 않도록 보정된다")
        void clampsLimit() throws Exception {
            saveRecruitment(author, "하나");

            mockMvc.perform(get("/team-recruitments").param("limit", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_count").value(1));
        }

        @Test
        @DisplayName("잘못된 정렬 값은 400 ILLEGAL_ARGUMENT 이다")
        void invalidSort() throws Exception {
            mockMvc.perform(get("/team-recruitments").param("sort", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ILLEGAL_ARGUMENT"));
        }

        @Test
        @DisplayName("잘못된 상태, 카테고리, 진행 방식 값도 400 ILLEGAL_ARGUMENT 이다")
        void invalidEnumParameters() throws Exception {
            for (String parameter : new String[] {"status", "categories", "meetingType"}) {
                mockMvc.perform(get("/team-recruitments").param(parameter, "UNKNOWN"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("ILLEGAL_ARGUMENT"));
            }
        }

        @Test
        @DisplayName("enum 영문 이름으로는 검색되지 않는다")
        void doesNotSearchEnumName() throws Exception {
            saveRecruitment(author, "가나다");

            mockMvc.perform(get("/team-recruitments").param("keyword", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_count").value(0));
        }
    }

    @Nested
    @DisplayName("GET /team-recruitments/{recruitmentId}")
    class GetRecruitment {

        @Test
        @DisplayName("비로그인 조회는 사용자별 필드가 비어 있다")
        void anonymousDetail() throws Exception {
            TeamRecruitment recruitment = saveRecruitment(author, "상세");

            mockMvc.perform(get("/team-recruitments/{id}", recruitment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.is_author").value(false))
                .andExpect(jsonPath("$.can_apply").value(false))
                .andExpect(jsonPath("$.apply_block_reason").value("LOGIN_REQUIRED"))
                .andExpect(jsonPath("$.application").doesNotExist())
                .andExpect(jsonPath("$.team_chat_available").value(false));
        }

        @Test
        @DisplayName("CLOSED 모집글은 미래 마감일이어도 d_day가 없다")
        void closedFutureDeadlineHasNoDday() throws Exception {
            TeamRecruitment recruitment = saveRecruitment(author, "마감된 상세");
            recruitment.close();
            entityManager.flush();

            mockMvc.perform(get("/team-recruitments/{id}", recruitment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(CLOSED.name()))
                .andExpect(jsonPath("$.d_day").doesNotExist());
        }

        @Test
        @DisplayName("작성자 조회는 관리 권한과 팀 채팅방 정보를 받는다")
        void authorDetail() throws Exception {
            mockMvc.perform(post("/team-recruitments")
                    .header("Authorization", "Bearer " + authorToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(generalBody(5)))
                .andExpect(status().isCreated());
            TeamRecruitment saved = recruitmentRepository
                .findAllByAuthor_Id(author.getUser().getId(), org.springframework.data.domain.Pageable.unpaged())
                .getContent().get(0);

            mockMvc.perform(get("/team-recruitments/{id}", saved.getId())
                    .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.is_author").value(true))
                .andExpect(jsonPath("$.can_manage_applicants").value(true))
                .andExpect(jsonPath("$.apply_block_reason").value("OWN_RECRUITMENT"))
                .andExpect(jsonPath("$.team_chat_available").value(true))
                .andExpect(jsonPath("$.team_chat_room_id").isNumber());
        }

        @Test
        @DisplayName("존재하지 않는 모집글은 404 이다")
        void notFound() throws Exception {
            mockMvc.perform(get("/team-recruitments/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TEAM_RECRUITMENT_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("GET /team-recruitments/me/created")
    class GetMyCreated {

        @Test
        @DisplayName("내가 작성한 모집글만 작성자 화면 필드와 함께 반환한다")
        void listsOnlyMine() throws Exception {
            mockMvc.perform(post("/team-recruitments")
                    .header("Authorization", "Bearer " + authorToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(generalBody(5)))
                .andExpect(status().isCreated());
            saveRecruitment(otherStudent, "남의 글");

            mockMvc.perform(get("/team-recruitments/me/created")
                    .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_count").value(1))
                .andExpect(jsonPath("$.recruitments[0].applicant_count").value(0))
                .andExpect(jsonPath("$.recruitments[0].can_close").value(true))
                .andExpect(jsonPath("$.recruitments[0].team_chat_available").value(true))
                .andExpect(jsonPath("$.recruitments[0].team_chat_room_id").isNumber());
        }

        @Test
        @DisplayName("미인증 요청은 401 이다")
        void unauthenticated() throws Exception {
            mockMvc.perform(get("/team-recruitments/me/created"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("잘못된 정렬 값은 400 ILLEGAL_ARGUMENT 이다")
        void invalidSort() throws Exception {
            mockMvc.perform(get("/team-recruitments/me/created")
                    .header("Authorization", "Bearer " + authorToken)
                    .param("sort", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ILLEGAL_ARGUMENT"));
        }
    }

    @Nested
    @DisplayName("작성자만 가능한 요청")
    class AuthorOnly {

        @Test
        @DisplayName("타인은 수정할 수 없다")
        void otherCannotUpdate() throws Exception {
            TeamRecruitment recruitment = saveRecruitment(author, "내 글");

            mockMvc.perform(put("/team-recruitments/{id}", recruitment.getId())
                    .header("Authorization", "Bearer " + otherToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(generalBody(5)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("TEAM_RECRUITMENT_FORBIDDEN"));
        }

        @Test
        @DisplayName("타인은 삭제할 수 없다")
        void otherCannotDelete() throws Exception {
            TeamRecruitment recruitment = saveRecruitment(author, "내 글");

            mockMvc.perform(delete("/team-recruitments/{id}", recruitment.getId())
                    .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("타인은 마감할 수 없다")
        void otherCannotClose() throws Exception {
            TeamRecruitment recruitment = saveRecruitment(author, "내 글");

            mockMvc.perform(put("/team-recruitments/{id}/close", recruitment.getId())
                    .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("미인증 삭제 요청은 401 이다")
        void unauthenticatedDelete() throws Exception {
            TeamRecruitment recruitment = saveRecruitment(author, "내 글");

            mockMvc.perform(delete("/team-recruitments/{id}", recruitment.getId()))
                .andExpect(status().isUnauthorized());
        }
    }
}
