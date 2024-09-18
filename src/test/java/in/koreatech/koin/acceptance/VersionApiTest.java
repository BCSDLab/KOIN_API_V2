package in.koreatech.koin.acceptance;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.repository.VersionRepository;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VersionApiTest extends AcceptanceTest {

    @Autowired
    private VersionRepository versionRepository;

    @BeforeAll
    void setup() {
        clear();
    }

    @Test
    void 버전_타입을_통해_버전_정보를_조회한다() throws Exception {
        Version version = versionRepository.save(
            Version.builder()
                .version("1.0.0")
                .type(VersionType.TIMETABLE.getValue())
                .build()
        );

        mockMvc.perform(
                get("/versions/" + version.getType())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                {
                    "id": %d,
                    "version": "1.0.0",
                    "type": "timetable",
                    "created_at": "2024-01-15 12:00:00",
                    "updated_at": "2024-01-15"
                }
                """, version.getId()
            )));
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
}
