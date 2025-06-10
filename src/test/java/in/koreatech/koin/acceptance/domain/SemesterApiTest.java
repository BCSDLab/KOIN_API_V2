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
import in.koreatech.koin.domain.graduation.model.CourseType;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.acceptance.fixture.CourseTypeAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.LectureAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.SemesterAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.TimeTableV2AcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SemesterApiTest extends AcceptanceTest {

    @Autowired
    private LectureAcceptanceFixture lectureFixture;

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private SemesterAcceptanceFixture semesterFixture;

    @Autowired
    private TimeTableV2AcceptanceFixture timetableV2Fixture;

    @Autowired
    private CourseTypeAcceptanceFixture courseTypeFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    @BeforeAll
    void setup() {
        clear();
    }

    @Test
    void 모든_학기를_조회한다() throws Exception {
        semesterFixture.semester_2024년도_1학기();
        semesterFixture.semester_2024년도_2학기();
        semesterFixture.semester_2024년도_여름();
        semesterFixture.semester_2023년도_1학기();
        semesterFixture.semester_2023년도_2학기();
        semesterFixture.semester_2023년도_여름();
        semesterFixture.semester_2023년도_겨울();

        mockMvc.perform(
                get("/semesters")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [
                    {
                        "id": 2,
                        "semester": "20242"
                    },
                    {
                        "id": 3,
                        "semester": "2024-여름"
                    },
                    {
                        "id": 1,
                        "semester": "20241"
                    },
                    {
                        "id": 7,
                        "semester": "2023-겨울"
                    },
                    {
                        "id": 5,
                        "semester": "20232"
                    },
                    {
                        "id": 6,
                        "semester": "2023-여름"
                    },
                    {
                        "id": 4,
                        "semester": "20231"
                    }
                ]
                """));
    }

    @Test
    void 학생이_가진_시간표의_학기를_조회한다() throws Exception {
        Department department = departmentFixture.컴퓨터공학부();
        User user = userFixture.준호_학생(department, null).getUser();
        String token = userFixture.getToken(user);
        Semester semester1 = semesterFixture.semester_2019년도_2학기();
        Semester semester2 = semesterFixture.semester_2020년도_1학기();
        Lecture HRD_개론 = lectureFixture.HRD_개론(semester1.getSemester());
        Lecture 건축구조의_이해_및_실습 = lectureFixture.건축구조의_이해_및_실습(semester2.getSemester());
        CourseType courseType1 = courseTypeFixture.HRD_필수();
        CourseType courseType2 = courseTypeFixture.전공_필수();
        timetableV2Fixture.시간표6(user, semester1, HRD_개론, null, courseType1, null);
        timetableV2Fixture.시간표6(user, semester2, 건축구조의_이해_및_실습, null, courseType2, null);

        mockMvc.perform(
                get("/semesters/check")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "semesters": [
                      "20201",
                      "20192"
                    ]
                }
                """));
    }

    @Test
    void 모든_학기를_조회한다_V3() throws Exception {
        semesterFixture.semester_2019년도_1학기();
        semesterFixture.semester_2019년도_2학기();
        semesterFixture.semester_2019년도_여름();
        semesterFixture.semester_2019년도_겨울();
        semesterFixture.semester_2020년도_1학기();
        semesterFixture.semester_2020년도_2학기();
        semesterFixture.semester_2020년도_여름();
        semesterFixture.semester_2020년도_겨울();

        mockMvc.perform(
                get("/v3/semesters")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [
                    {
                        "year": 2020,
                        "term": "겨울학기"
                    },
                    {
                        "year": 2020,
                        "term": "2학기"
                    },
                    {
                        "year": 2020,
                        "term": "여름학기"
                    },
                    {
                        "year": 2020,
                        "term": "1학기"
                    },
                    {
                        "year": 2019,
                        "term": "겨울학기"
                    },
                    {
                        "year": 2019,
                        "term": "2학기"
                    },
                    {
                        "year": 2019,
                        "term": "여름학기"
                    },
                    {
                        "year": 2019,
                        "term": "1학기"
                    }
                ]
                """));
    }

    @Test
    void 학생이_가진_시간표의_학기를_조회한다_V3() throws Exception {
        Department department = departmentFixture.컴퓨터공학부();
        User user = userFixture.준호_학생(department, null).getUser();
        String token = userFixture.getToken(user);
        Semester semester1 = semesterFixture.semester_2019년도_2학기();
        Semester semester2 = semesterFixture.semester_2020년도_1학기();
        Lecture HRD_개론 = lectureFixture.HRD_개론(semester1.getSemester());
        Lecture 건축구조의_이해_및_실습 = lectureFixture.건축구조의_이해_및_실습(semester2.getSemester());
        CourseType courseType1 = courseTypeFixture.HRD_필수();
        CourseType courseType2 = courseTypeFixture.전공_필수();
        timetableV2Fixture.시간표6(user, semester1, HRD_개론, null, courseType1, null);
        timetableV2Fixture.시간표6(user, semester2, 건축구조의_이해_및_실습, null, courseType2, null);

        mockMvc.perform(
                get("/v3/semesters/check")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "semesters": [
                    {
                        "year": 2019,
                        "term": "2학기"
                    },
                    {
                        "year": 2020,
                        "term": "1학기"
                    }
                    ]
                }
                """));
    }
}
