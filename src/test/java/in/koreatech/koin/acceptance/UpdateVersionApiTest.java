package in.koreatech.koin.acceptance;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.admin.updateversion.repository.AdminUpdateHistoryRepository;
import in.koreatech.koin.admin.updateversion.repository.AdminUpdateVersionRepository;
import in.koreatech.koin.domain.updateversion.model.UpdateVersion;
import in.koreatech.koin.domain.updateversion.model.UpdateVersionType;
import in.koreatech.koin.domain.updateversion.repository.UpdateVersionRepository;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.fixture.UpdateVersionFixture;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class UpdateVersionApiTest extends AcceptanceTest {

    @Autowired
    private UpdateVersionFixture updateVersionFixture;

    private UpdateVersion android;

    @BeforeAll
    void setup() {
        clear();
        android = updateVersionFixture.Android();
    }

    @Test
    void 업데이트_버전_타입을_통해_최소_버전_정보를_조회한다() throws Exception {
        mockMvc.perform(
                get("/update/version/" + android.getType())
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
    void 버전_타입을_통해_버전_정보를_조회한다_저장되지_않은_버전_타입을_요청한_경우_에러가_발생한다() throws Exception {
        UpdateVersionType failureType = UpdateVersionType.IOS;

        mockMvc.perform(
                get("/update/version/" + failureType)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNotFound());
    }
}
