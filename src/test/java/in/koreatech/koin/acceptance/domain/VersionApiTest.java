package in.koreatech.koin.acceptance.domain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.VersionAcceptanceFixture;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionType;

class VersionApiTest extends AcceptanceTest {

    @Autowired
    private VersionAcceptanceFixture versionFixture;

    private Version android;

    @BeforeAll
    void setup() {
        clear();
        android = versionFixture.android();
    }

    @Test
    void 버전_타입을_통해_버전_정보를_조회한다_저장되지_않은_버전_타입을_요청한_경우_에러가_발생한다() throws Exception {
        VersionType failureType = VersionType.TIMETABLE;
        String undefinedType = "undefined";

        mockMvc.perform(
                get("/versions/" + failureType.getValue())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNotFound());

        mockMvc.perform(
                get("/versions/" + undefinedType)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNotFound());
    }

    @Test
    void 타입을_통해_최소_버전_정보를_조회한다() throws Exception {
        mockMvc.perform(
                get("/version/" + android.getType())
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
    void 타입을_통해_버전_정보를_조회한다_저장되지_않은_버전_타입을_요청한_경우_에러가_발생한다() throws Exception {
        VersionType failureType = VersionType.IOS;

        mockMvc.perform(
                get("/version/" + failureType)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNotFound());
    }
}
