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
import in.koreatech.koin.fixture.LectureFixture;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LectureApiTest extends AcceptanceTest {

    @Autowired
    private LectureFixture lectureFixture;

    @BeforeAll
    void setup() {
        clear();
    }

    @Test
    void 특정_학기_강의를_조회한다() throws Exception {
        String semester = "20201";
        lectureFixture.HRD_개론(semester);

        mockMvc.perform(
                get("/v3/lectures")
                    .param("semester_date", semester)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [
                    {
                        "id" : 1,
                        "code": "BSM590",
                        "name": "컴퓨팅사고",
                        "grades": "3",
                        "lecture_class": "06",
                        "regular_number": "22",
                        "department": "기계공학부",
                        "target": "기공1",
                        "professor": "박한수,최준호",
                        "is_english": "",
                        "design_score": "0",
                        "is_elearning": "",
                        "lecture_infos": [
                          {
                            "week": 0,
                            "start_time": 12,
                            "end_time": 15
                          },
                          {
                            "week": 2,
                            "start_time": 210,
                            "end_time": 213
                          }
                        ]
                    }
                ]
                """));
    }
}
