package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.admin.abtest.model.Abtest;
import in.koreatech.koin.admin.abtest.model.AbtestVariable;
import in.koreatech.koin.admin.abtest.model.AccessHistoryAbtestVariable;
import in.koreatech.koin.admin.abtest.model.Device;
import in.koreatech.koin.admin.abtest.repository.AbtestRepository;
import in.koreatech.koin.admin.abtest.repository.DeviceRepository;
import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.acceptance.fixture.AbtestAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.DeviceAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;

@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AbtestApiTest extends AcceptanceTest {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private AbtestAcceptanceFixture abtestFixture;

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private DeviceAcceptanceFixture deviceFixture;

    @Autowired
    private AbtestRepository abtestRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    private Admin admin;
    private String adminToken;
    private Department department;

    @BeforeAll
    void setUp() {
        clear();
        admin = userFixture.코인_운영자();
        adminToken = userFixture.getToken(admin.getUser());
        department = departmentFixture.컴퓨터공학부();
    }

    @Test
    void 실험을_생성한다() throws Exception {
        mockMvc.perform(
                post("/abtest")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminToken)
                    .content("""
                        {
                          "display_title": "사장님 전화번호 회원가입 실험",
                          "creator": "송선권",
                          "team": "campus",
                          "description": "세부설명",
                          "status": "IN_PROGRESS",
                          "title": "business.register.phone_number",
                          "variables": [
                            {
                              "rate": 33,
                              "display_name": "실험군 A",
                              "name": "A"
                            },
                            {
                              "rate": 33,
                              "display_name": "실험군 B",
                              "name": "B"
                            },
                            {
                              "rate": 34,
                              "display_name": "실험군 C",
                              "name": "C"
                            }
                          ]
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                  "id": 1,
                  "display_title": "사장님 전화번호 회원가입 실험",
                  "creator": "송선권",
                  "team": "campus",
                  "description": "세부설명",
                  "title": "business.register.phone_number",
                  "status": "IN_PROGRESS",
                  "winner_name": null,
                  "variables": [
                    {
                      "rate": 33,
                      "display_name": "실험군 A",
                      "name": "A"
                    },
                    {
                      "rate": 33,
                      "display_name": "실험군 B",
                      "name": "B"
                    },
                    {
                      "rate": 34,
                      "display_name": "실험군 C",
                      "name": "C"
                    }
                  ],
                  "created_at": "2024-01-15 12:00:00",
                  "updated_at": "2024-01-15 12:00:00"
                }
                """));
    }

    @Test
    void 실험을_단건_조회한다() throws Exception {
        Abtest abtest = abtestFixture.식단_UI_실험();

        mockMvc.perform(
                get("/abtest/{id}", abtest.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminToken)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                  "id": 1,
                  "display_title": "식단_UI_실험",
                  "creator": "송선권",
                  "team": "campus",
                  "description": "세부설명",
                  "title": "dining_ui_test",
                  "status": "IN_PROGRESS",
                  "winner_name": null,
                  "variables": [
                    {
                      "rate": 50,
                      "display_name": "실험군 A",
                      "name": "A"
                    },
                    {
                      "rate": 50,
                      "display_name": "실험군 B",
                      "name": "B"
                    }
                  ],
                  "created_at": "2024-01-15 12:00:00",
                  "updated_at": "2024-01-15 12:00:00"
                }
                """));
    }

    @Test
    void 실험_목록을_조회한다() throws Exception {
        Abtest abtest1 = abtestFixture.식단_UI_실험();
        Abtest abtest2 = abtestFixture.주변상점_UI_실험();

        mockMvc.perform(
                get("/abtest")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminToken)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                  "total_count": 2,
                  "current_count": 2,
                  "total_page": 1,
                  "current_page": 1,
                  "tests": [
                    {
                      "id": 2,
                      "status": "IN_PROGRESS",
                      "creator": "송선권",
                      "team": "campus",
                      "display_title": "주변상점_UI_실험",
                      "title": "shop_ui_test",
                      "winner_name": null,
                      "created_at": "2024-01-15 12:00:00",
                      "updated_at": "2024-01-15 12:00:00"
                    },
                    {
                      "id": 1,
                      "status": "IN_PROGRESS",
                      "creator": "송선권",
                      "team": "campus",
                      "display_title": "식단_UI_실험",
                      "title": "dining_ui_test",
                      "winner_name": null,
                      "created_at": "2024-01-15 12:00:00",
                      "updated_at": "2024-01-15 12:00:00"
                    }
                  ]
                }
                """));
    }

    @Test
    void 실험_목록을_조회한다_페이지네이션() throws Exception {
        for (int i = 0; i < 10; i++) {
            abtestFixture.식단_UI_실험(i);
        }

        mockMvc.perform(
                get("/abtest?page=2&limit=8")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminToken)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                  "total_count": 10,
                  "current_count": 2,
                  "total_page": 2,
                  "current_page": 2,
                  "tests": [
                    {
                      "id": 2,
                      "status": "IN_PROGRESS",
                      "creator": "송선권",
                      "team": "campus",
                      "display_title": "식단_UI_실험",
                      "title": "dining_ui_test1",
                      "winner_name": null,
                      "created_at": "2024-01-15 12:00:00",
                      "updated_at": "2024-01-15 12:00:00"
                    },
                    {
                      "id": 1,
                      "status": "IN_PROGRESS",
                      "creator": "송선권",
                      "team": "campus",
                      "display_title": "식단_UI_실험",
                      "title": "dining_ui_test0",
                      "winner_name": null,
                      "created_at": "2024-01-15 12:00:00",
                      "updated_at": "2024-01-15 12:00:00"
                    }
                  ]
                }
                """));
    }

    @Test
    void 실험을_수정한다() throws Exception {
        Abtest abtest = abtestFixture.식단_UI_실험();

        mockMvc.perform(
                put("/abtest/{id}", abtest.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminToken)
                    .content("""
                        {
                          "display_title": "식단_UI_실험",
                          "creator": "김성재",
                          "team": "user",
                          "title": "dining_ui_test",
                          "description": "세부설명2",
                          "variables": [
                            {
                              "rate": 10,
                              "display_name": "실험군 A",
                              "name": "A"
                            },
                            {
                              "rate": 90,
                              "display_name": "실험군 B",
                              "name": "B"
                            }
                          ],
                          "created_at": "2024-01-15 12:00:00",
                          "updated_at": "2024-01-15 12:00:00"
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                  "id": 1,
                  "display_title": "식단_UI_실험",
                  "creator": "김성재",
                  "team": "user",
                  "title": "dining_ui_test",
                  "status": "IN_PROGRESS",
                  "description": "세부설명2",
                  "winner_name": null,
                  "variables": [
                    {
                      "rate": 10,
                      "display_name": "실험군 A",
                      "name": "A"
                    },
                    {
                      "rate": 90,
                      "display_name": "실험군 B",
                      "name": "B"
                    }
                  ],
                  "created_at": "2024-01-15 12:00:00",
                  "updated_at": "2024-01-15 12:00:00"
                }
                """));
    }

    @Test
    void 실험을_삭제한다() throws Exception {
        Abtest abtest = abtestFixture.식단_UI_실험();

        mockMvc.perform(
                delete("/abtest/{id}", abtest.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminToken)
            )
            .andExpect(status().isNoContent());

        assertThat(abtestRepository.findById(abtest.getId())).isNotPresent();
    }

    @Test
    void 실험을_종료한다() throws Exception {
        final Abtest abtest = abtestFixture.식단_UI_실험();
        String winner = "A";

        mockMvc.perform(
                post("/abtest/close/{id}", abtest.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminToken)
                    .content(String.format("""
                        {
                          "winner_name": "%s"
                        }
                        """, winner))
            )
            .andExpect(status().isOk());

        transactionTemplate.executeWithoutResult(status -> {
            Abtest result = abtestRepository.getById(abtest.getId());
            assertSoftly(
                softly -> {
                    softly.assertThat(result.getStatus().name()).isEqualTo("CLOSED");
                    softly.assertThat(result.getWinner().getName()).isEqualTo(winner);
                }
            );
        });
    }

    @Test
    void 실험군_수동편입_이름으로_유저_목록을_조회한다() throws Exception {
        Student student = userFixture.성빈_학생(department);
        Owner owner = userFixture.성빈_사장님();

        mockMvc.perform(
                get("/abtest/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminToken)
                    .param("name", student.getUser().getName())
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                  "users": [
                    {
                    "id": 2,
                    "name" : "박성빈",
                    "detail": "testsungbeen@koreatech.ac.kr"
                    },
                    {
                    "id": 3,
                    "name" : "박성빈",
                    "detail": null
                    }
                  ]
                }
                """));
    }

    @Test
    void 실험군_수동편입_유저_ID로_기기_목록을_조회한다() throws Exception {
        Student student = userFixture.성빈_학생(department);
        Device device1 = deviceFixture.아이폰(student.getUser().getId());
        Device device2 = deviceFixture.갤럭시(student.getUser().getId());

        mockMvc.perform(
                get("/abtest/user/{userId}/device", student.getUser().getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminToken)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                  "devices": [
                    {
                    "id": 1,
                    "type": "mobile",
                    "model" : "아이폰14",
                    "last_accessed_at": "2024-01-15"
                    },
                    {
                    "id": 2,
                    "type": "mobile",
                    "model" : "갤럭시24",
                    "last_accessed_at": "2024-01-15"
                    }
                  ]
                }
                """));
    }

    @Test
    void 특정_유저의_실험군을_수동으로_편입시킨다() throws Exception {
        Student student = userFixture.성빈_학생(department);
        Device device = deviceFixture.아이폰(student.getUser().getId());
        Abtest abtest = abtestFixture.식단_UI_실험();

        mockMvc.perform(
                post("/abtest/{id}/move", abtest.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminToken)
                    .content(String.format("""
                        {
                          "device_id": %d,
                          "variable_name": "A"
                        }
                        """, device.getId()))
            )
            .andExpect(status().isOk());

        transactionTemplate.executeWithoutResult(status -> {
            assertSoftly(
                softly -> {
                    Device result = deviceRepository.getById(device.getId());
                    Optional<AbtestVariable> variable = result.getAccessHistory()
                        .getAccessHistoryAbtestVariables()
                        .stream()
                        .map(AccessHistoryAbtestVariable::getVariable)
                        .filter(var -> var.getAbtest().getTitle().equals(abtest.getTitle()))
                        .findAny();
                    softly.assertThat(variable.get().getName()).isEqualTo("A");
                }
            );
        });
    }

    @Test
    void 자신의_실험군을_조회한다() throws Exception {
        Student student = userFixture.성빈_학생(department);
        final Device device = deviceFixture.아이폰(student.getUser().getId());
        Abtest abtest = abtestFixture.식단_UI_실험();

        mockMvc.perform(
            post("/abtest/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .header("access_history_id", device.getAccessHistory().getId())
                .content(String.format("""
                    {
                      "title": "dining_ui_test"
                    }
                    """))
        );

        MvcResult mvcResult = mockMvc.perform(
                post("/abtest/assign")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("access_history_id", device.getAccessHistory().getId())
                    .content(String.format("""
                        {
                          "title": "dining_ui_test"
                        }
                        """))
            )
            .andExpect(status().isOk())
            .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();

        transactionTemplate.executeWithoutResult(status -> {
            assertSoftly(
                softly -> {
                    Device result = deviceRepository.getById(device.getId());
                    Optional<AbtestVariable> variable = result.getAccessHistory()
                        .getAccessHistoryAbtestVariables()
                        .stream()
                        .map(AccessHistoryAbtestVariable::getVariable)
                        .filter(var -> var.getAbtest().getTitle().equals(abtest.getTitle()))
                        .findAny();
                    softly.assertThat(responseBody).isEqualTo(
                        String.format("{\"variable_name\":\"A\",\"access_history_id\":1}",
                            variable.get().getName(), result.getAccessHistory().getId()));
                }
            );
        });
    }

    @Test
    void 실험군_자동_편입_실험군에_최초로_편입된다() throws Exception {
        Student student = userFixture.성빈_학생(department);
        Device device1 = deviceFixture.아이폰(student.getUser().getId());
        Device device2 = deviceFixture.갤럭시(student.getUser().getId());
        Abtest abtest = abtestFixture.식단_UI_실험();

        MvcResult mvcResult = mockMvc.perform(
                post("/abtest/assign")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("access_history_id", device1.getAccessHistory().getId())
                    .content(String.format("""
                        {
                          "title": "dining_ui_test"
                        }
                        """))
            )
            .andExpect(status().isOk())
            .andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();

        MvcResult mvcResult2 = mockMvc.perform(
                post("/abtest/assign")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("access_history_id", device2.getAccessHistory().getId())
                    .content(String.format("""
                        {
                          "title": "dining_ui_test"
                        }
                        """))
            )
            .andExpect(status().isOk())
            .andReturn();
        String responseBody2 = mvcResult2.getResponse().getContentAsString();
        assertNotEquals(responseBody, responseBody2);
    }
}
