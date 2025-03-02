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
import in.koreatech.koin.fixture.SemesterFixture;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TimetableApiTest extends AcceptanceTest {

    @Autowired
    private LectureFixture lectureFixture;

    @Autowired
    private SemesterFixture semesterFixture;

    @BeforeAll
    void setup() {
        clear();
    }

    @Test
    void 특정_학기_강의를_조회한다() throws Exception {
        semesterFixture.semester_2019년도_2학기();
        semesterFixture.semester_2020년도_1학기();
        String semester = "20201";
        lectureFixture.HRD_개론(semester);
        lectureFixture.건축구조의_이해_및_실습("20192");

        mockMvc.perform(
                get("/lectures")
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
                        "class_time": [
                           12, 13, 14, 15, 210, 211, 212, 213
                        ]
                    }
                ]
                """));
    }

    @Test
    void 특정_학기_강의들을_조회한다() throws Exception {
        semesterFixture.semester_2020년도_1학기();
        String semester = "20201";
        lectureFixture.HRD_개론(semester);
        lectureFixture.건축구조의_이해_및_실습(semester);
        lectureFixture.재료역학(semester);

        mockMvc.perform(
                get("/lectures")
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
                        "class_time": [
                            12, 13, 14, 15, 210, 211, 212, 213
                        ]
                    },
                    {
                        "id" : 2,
                        "code": "ARB244",
                        "name": "건축구조의 이해 및 실습",
                        "grades": "3",
                        "lecture_class": "01",
                        "regular_number": "25",
                        "department": "디자인ㆍ건축공학부",
                        "target": "디자 1 건축",
                        "professor": "황현식",
                        "is_english": "N",
                        "design_score": "0",
                        "is_elearning": "N",
                        "class_time": [
                            200, 201, 202, 203, 204, 205, 206, 207
                        ]
                    },
                    {
                        "id" : 3,
                        "code": "MEB311",
                        "name": "재료역학",
                        "grades": "3",
                        "lecture_class": "01",
                        "regular_number": "35",
                        "department": "기계공학부",
                        "target": "기공전체",
                        "professor": "허준기",
                        "is_english": "",
                        "design_score": "0",
                        "is_elearning": "",
                        "class_time": [
                            100, 101, 102, 103, 308, 309
                        ]
                    }
                ]
                """));
    }

    @Test
    void 존재하지_않는_학기를_조회하면_404() throws Exception {
        String semester = "20201";
        lectureFixture.HRD_개론(semester);
        lectureFixture.건축구조의_이해_및_실습(semester);

        mockMvc.perform(
                get("/lectures")
                    .param("semester_date", "20193")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNotFound());
    }

    @Test
    void 계절학기를_조회하면_빈_리스트로_반환한다() throws Exception {
        semesterFixture.semester_2024년도_1학기();
        semesterFixture.semester_2024년도_2학기();
        semesterFixture.semester_2024년도_여름();
        semesterFixture.semester_2024년도_겨울();

        mockMvc.perform(
                get("/lectures")
                    .param("semester_date", "2024-여름")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }
}
