package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentProfileActivity;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentProfileSkill;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentProfileActivityRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentProfileRepository;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentProfileSkillRepository;

/**
 * 팀원 모집 프로필 API 의 요청/응답 계약과 upsert 동작을 확인한다.
 */
class TeamRecruitmentProfileApiTest extends AcceptanceTest {

    private static final String URL = "/team-recruitment-profiles/me";

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    @Autowired
    private TeamRecruitmentProfileRepository profileRepository;

    @Autowired
    private TeamRecruitmentProfileSkillRepository skillRepository;

    @Autowired
    private TeamRecruitmentProfileActivityRepository activityRepository;

    private Student student;
    private String token;

    @BeforeEach
    void setUp() {
        clear();
        Department department = departmentFixture.컴퓨터공학부();
        student = userFixture.준호_학생(department, null);
        token = userFixture.getToken(student.getUser());
    }

    private static final String UPSERT_BODY = """
        {
          "profile_nickname": "홍길동",
          "preferred_role": "기획",
          "skills": ["정보처리기사", "SQLD", "피그마"],
          "activities": [
            {
              "title": "AI 공모전",
              "started_at": "2025-03-03",
              "ended_at": "2025-05-05",
              "is_ongoing": false,
              "description": "기획 담당"
            },
            {
              "title": "교내 동아리",
              "started_at": "2025-06-01",
              "ended_at": null,
              "is_ongoing": true,
              "description": "진행 중"
            }
          ],
          "self_introduction": "안녕하세요."
        }
        """;

    private static final String UPDATED_BODY = """
        {
          "profile_nickname": "수정된닉",
          "preferred_role": "백엔드",
          "skills": ["Java"],
          "activities": [
            {
              "title": "오픈소스",
              "started_at": "2026-01-01",
              "ended_at": "2026-02-01",
              "is_ongoing": false,
              "description": "기여"
            }
          ],
          "self_introduction": "수정했습니다."
        }
        """;

    private static String activityBody(String endedAt, boolean isOngoing) {
        return """
            {
              "profile_nickname": "홍길동",
              "preferred_role": "기획",
              "skills": [],
              "activities": [
                {
                  "title": "활동",
                  "started_at": "2025-03-03",
                  "ended_at": %s,
                  "is_ongoing": %s,
                  "description": "설명"
                }
              ],
              "self_introduction": "소개"
            }
            """.formatted(endedAt, isOngoing);
    }

    @Nested
    @DisplayName("GET /team-recruitment-profiles/me")
    class GetProfile {

        @Test
        @DisplayName("프로필이 없으면 404 이다")
        void notFoundWhenAbsent() throws Exception {
            mockMvc.perform(get(URL).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TEAM_RECRUITMENT_PROFILE_NOT_FOUND"));
        }

        @Test
        @DisplayName("미인증 요청은 401 이다")
        void unauthenticated() throws Exception {
            mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("저장한 프로필을 학적 정보와 함께 반환한다")
        void returnsProfileWithAcademicInfo() throws Exception {
            mockMvc.perform(put(URL)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(UPSERT_BODY))
                .andExpect(status().isOk());

            mockMvc.perform(get(URL).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile_nickname").value("홍길동"))
                .andExpect(jsonPath("$.department").value("컴퓨터공학부"))
                .andExpect(jsonPath("$.student_number").value(student.getStudentNumber()))
                .andExpect(jsonPath("$.preferred_role").value("기획"))
                .andExpect(jsonPath("$.skills.length()").value(3))
                .andExpect(jsonPath("$.activities.length()").value(2))
                .andExpect(jsonPath("$.self_introduction").value("안녕하세요."));
        }
    }

    @Nested
    @DisplayName("PUT /team-recruitment-profiles/me")
    class UpsertProfile {

        @Test
        @DisplayName("프로필을 생성하고 활동 id 를 채워서 반환한다")
        void createsProfile() throws Exception {
            mockMvc.perform(put(URL)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(UPSERT_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activities[0].id").isNumber())
                .andExpect(jsonPath("$.activities[0].title").value("AI 공모전"))
                .andExpect(jsonPath("$.activities[0].is_ongoing").value(false))
                .andExpect(jsonPath("$.activities[1].ended_at").doesNotExist())
                .andExpect(jsonPath("$.activities[1].is_ongoing").value(true));

            Integer userId = student.getUser().getId();
            assertThat(skillRepository.findAllByProfile_UserIdOrderByDisplayOrderAsc(userId))
                .extracting(TeamRecruitmentProfileSkill::getDisplayOrder)
                .containsExactly(1, 2, 3);
            assertThat(activityRepository.findAllByProfile_UserIdOrderByDisplayOrderAsc(userId))
                .extracting(TeamRecruitmentProfileActivity::getDisplayOrder)
                .containsExactly(1, 2);
        }

        @Test
        @DisplayName("재요청은 기술과 활동을 전부 대체한다")
        void replacesSkillsAndActivities() throws Exception {
            mockMvc.perform(put(URL)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(UPSERT_BODY))
                .andExpect(status().isOk());

            mockMvc.perform(put(URL)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(UPDATED_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile_nickname").value("수정된닉"))
                .andExpect(jsonPath("$.skills.length()").value(1))
                .andExpect(jsonPath("$.activities.length()").value(1));

            Integer userId = student.getUser().getId();
            assertThat(profileRepository.findByUser_Id(userId)).isPresent();
            assertThat(skillRepository.findAllByProfile_UserIdOrderByDisplayOrderAsc(userId)).hasSize(1);
            assertThat(activityRepository.findAllByProfile_UserIdOrderByDisplayOrderAsc(userId)).hasSize(1);
        }

        @Test
        @DisplayName("같은 요청을 반복해도 성공한다")
        void isIdempotent() throws Exception {
            for (int attempt = 0; attempt < 2; attempt++) {
                mockMvc.perform(put(URL)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATED_BODY))
                    .andExpect(status().isOk());
            }
        }

        @Test
        @DisplayName("미인증 요청은 401 이다")
        void unauthenticated() throws Exception {
            mockMvc.perform(put(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(UPSERT_BODY))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("진행 중이 아닌 활동에 종료일이 없으면 400 이다")
        void endDateRequired() throws Exception {
            mockMvc.perform(put(URL)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(activityBody("null", false)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TEAM_RECRUITMENT_ACTIVITY_END_DATE_REQUIRED"));
        }

        @Test
        @DisplayName("진행 중인 활동에 종료일이 있으면 400 이다")
        void endDateMustBeNull() throws Exception {
            mockMvc.perform(put(URL)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(activityBody("\"2025-05-05\"", true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TEAM_RECRUITMENT_ACTIVITY_END_DATE_MUST_BE_NULL"));
        }

        @Test
        @DisplayName("하루짜리 활동은 종료일이 시작일과 같아도 저장된다")
        void allowsSingleDayActivity() throws Exception {
            mockMvc.perform(put(URL)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(activityBody("\"2025-03-03\"", false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activities[0].ended_at").value("2025-03-03"));
        }

        @Test
        @DisplayName("종료일이 시작일보다 이전이면 400 이다")
        void endDateBeforeStartDate() throws Exception {
            mockMvc.perform(put(URL)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(activityBody("\"2025-03-02\"", false)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_START_DATE_AFTER_END_DATE"));
        }

        @Test
        @DisplayName("닉네임이 20자를 넘으면 400 이다")
        void nicknameTooLong() throws Exception {
            String body = """
                {
                  "profile_nickname": "%s",
                  "preferred_role": "기획",
                  "skills": [],
                  "activities": [],
                  "self_introduction": "소개"
                }
                """.formatted("가".repeat(21));

            mockMvc.perform(put(URL)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isBadRequest());
        }
    }
}
