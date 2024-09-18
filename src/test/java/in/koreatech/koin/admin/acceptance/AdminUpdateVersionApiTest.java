package in.koreatech.koin.admin.acceptance;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.admin.updateversion.repository.AdminUpdateVersionRepository;
import in.koreatech.koin.domain.updateversion.model.UpdateContent;
import in.koreatech.koin.domain.updateversion.model.UpdateVersion;
import in.koreatech.koin.domain.updateversion.model.UpdateVersionType;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.fixture.UpdateVersionFixture;
import in.koreatech.koin.fixture.UserFixture;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class AdminUpdateVersionApiTest extends AcceptanceTest {

    @Autowired
    private AdminUpdateVersionRepository adminUpdateVersionRepository;

    @Autowired
    private UpdateVersionFixture updateVersionFixture;

    @Autowired
    private UserFixture userFixture;

    private UpdateVersion android;
    private User admin;
    private String admin_token;

    @BeforeAll
    void setup() {
        clear();
        android = updateVersionFixture.Android();
        admin = userFixture.코인_운영자();
        admin_token = userFixture.getToken(admin);
    }

    @Test
    void 업데이트_버전_타입을_통해_최소_버전_정보를_조회한다() throws Exception {
        mockMvc.perform(
                get("/admin/update/version/" + android.getType())
                    .header("Authorization", "Bearer " + admin_token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                {
                  "id": 1,
                  "type": %s,
                  "version": "3.5.0",
                  "title": "코인 신기능 업데이트",
                  "body": [
                    {
                      "body_title": "Android 백그라운드 푸시 알림",
                      "body_content": "더 빠른 알림을 위해 업데이트 해주세요!"
                    },
                    {
                      "body_title": "Android 키워드 알림",
                      "body_content": "더 빠른 알림을 위해 업데이트 해주세요!"
                    }
                  ],
                  "created_at": "2024-01-15 12:00:00",
                  "updated_at": "2024-01-15"
                }
                """, android.getType()
            )));
    }

    @Test
    void 모든_타입의_최소_버전_정보를_조회한다() throws Exception {
        mockMvc.perform(
                get("/admin/update/version")
                    .header("Authorization", "Bearer " + admin_token)
                    .param("page", "1")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total_count").value(1))
            .andExpect(jsonPath("$.current_count").value(1))
            .andExpect(jsonPath("$.total_page").value(1))
            .andExpect(jsonPath("$.current_page").value(1))
            .andExpect(jsonPath("$.versions.length()").value(1));
    }

    @Test
    void 특정_타입의_최소_버전을_업데이트_한다() throws Exception {
        mockMvc.perform(
                post("/admin/update/version/" + android.getType())
                    .header("Authorization", "Bearer " + admin_token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "version": "3.6.1",
                              "title": "코인의 새로운 기능 업데이트",
                              "body": [
                                {
                                  "body_title": "백그라운드 푸시 알림",
                                  "body_content": "정확하고 빠른..."
                                }
                              ]
                            }
                        """)
            )
            .andExpect(status().isOk());

        UpdateVersion updatedVersion = adminUpdateVersionRepository.getByType(android.getType());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(updatedVersion.getType()).isEqualTo(android.getType());
            softly.assertThat(updatedVersion.getVersion()).isEqualTo("3.6.1");
            softly.assertThat(updatedVersion.getTitle()).isEqualTo("코인의 새로운 기능 업데이트");
            softly.assertThat(updatedVersion.getContents().get(0).getTitle()).isEqualTo("백그라운드 푸시 알림");
            softly.assertThat(updatedVersion.getContents().get(0).getContent()).isEqualTo("정확하고 빠른...");
        });
    }

    @Test
    void 버전_타입을_통해_버전_정보를_조회한다_저장되지_않은_버전_타입을_요청한_경우_에러가_발생한다() throws Exception {
        UpdateVersionType failureType = UpdateVersionType.IOS;

        mockMvc.perform(
                get("/admin/update/version/" + failureType)
                    .header("Authorization", "Bearer " + admin_token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNotFound());
    }
}
