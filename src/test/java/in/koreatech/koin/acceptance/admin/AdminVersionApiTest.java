package in.koreatech.koin.acceptance.admin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.VersionAcceptanceFixture;
import in.koreatech.koin.admin.operators.model.Admin;
import in.koreatech.koin.admin.version.repository.AdminVersionRepository;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionType;

public class AdminVersionApiTest extends AcceptanceTest {

    @Autowired
    private AdminVersionRepository adminVersionRepository;

    @Autowired
    private VersionAcceptanceFixture versionFixture;

    @Autowired
    private UserAcceptanceFixture userFixture;

    private Version android;
    private Admin admin;
    private String admin_token;

    @BeforeAll
    void setup() {
        clear();
        android = versionFixture.android();
        admin = userFixture.코인_운영자();
        admin_token = userFixture.getToken(admin.getUser());
    }

    @Test
    void 특정_타입의_버전_정보를_조회한다() throws Exception {
        mockMvc.perform(
                get("/admin/version/" + android.getType())
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
                  "content": "더 빠른 알림을 위해 업데이트 해주세요!",
                  "created_at": "2024-01-15 12:00:00",
                  "updated_at": "2024-01-15"
                }
                """, android.getType()
            )));
    }

    @Test
    void 모든_타입의_버전_정보를_조회한다() throws Exception {
        adminVersionRepository.save(versionFixture.ios());
        mockMvc.perform(
                get("/admin/version")
                    .header("Authorization", "Bearer " + admin_token)
                    .param("page", "1")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total_count").value(2))
            .andExpect(jsonPath("$.current_count").value(2))
            .andExpect(jsonPath("$.total_page").value(1))
            .andExpect(jsonPath("$.current_page").value(1))
            .andExpect(jsonPath("$.versions.length()").value(2));
    }

    @Test
    void 특정_타입의_버전을_업데이트_한다() throws Exception {
        mockMvc.perform(
                post("/admin/version/" + android.getType())
                    .header("Authorization", "Bearer " + admin_token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "version": "3.6.1",
                              "title": "코인의 새로운 기능 업데이트",
                              "content": "더 빠른 알림을 위해 업데이트 해주세요!"
                            }
                        """)
            )
            .andExpect(status().isCreated());

        Version newVersion = adminVersionRepository.getByTypeAndIsPrevious(VersionType.from(android.getType()), false);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(newVersion.getType()).isEqualTo(android.getType());
            softly.assertThat(newVersion.getVersion()).isEqualTo("3.6.1");
            softly.assertThat(newVersion.getTitle()).isEqualTo("코인의 새로운 기능 업데이트");
            softly.assertThat(newVersion.getContent()).isEqualTo("더 빠른 알림을 위해 업데이트 해주세요!");
        });
    }

    @Test
    void 타입을_통해_버전_정보를_조회한다_저장되지_않은_버전_타입을_요청한_경우_에러가_발생한다() throws Exception {
        VersionType failureType = VersionType.IOS;

        mockMvc.perform(
                get("/admin/version/" + failureType)
                    .header("Authorization", "Bearer " + admin_token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNotFound());
    }
}
