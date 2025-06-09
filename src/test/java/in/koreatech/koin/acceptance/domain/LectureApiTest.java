package in.koreatech.koin.acceptance.domain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.acceptance.fixture.LectureAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.SemesterAcceptanceFixture;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LectureApiTest extends AcceptanceTest {

    @Autowired
    private LectureAcceptanceFixture lectureFixture;

    @Autowired
    private SemesterAcceptanceFixture semesterFixture;

    @BeforeAll
    void setup() {
        clear();
    }

    @Test
    void 특정_학기_강의를_조회한다() throws Exception {
        Semester semester = semesterFixture.semester_2020년도_1학기();
        lectureFixture.HRD_개론(semester.getSemester());

        mockMvc.perform(
                get("/v3/lectures")
                    .param("year", String.valueOf(semester.getYear()))
                    .param("term", String.valueOf(semester.getTerm().getDescription()))
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
                            "day": 0,
                            "start_time": 12,
                            "end_time": 15
                          },
                          {
                            "day": 2,
                            "start_time": 210,
                            "end_time": 213
                          }
                        ]
                    }
                ]
                """));
    }
}
