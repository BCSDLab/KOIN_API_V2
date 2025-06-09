package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV2.repository.TimetableLectureRepositoryV2;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.acceptance.fixture.DepartmentFixture;
import in.koreatech.koin.acceptance.fixture.LectureFixture;
import in.koreatech.koin.acceptance.fixture.SemesterFixture;
import in.koreatech.koin.acceptance.fixture.TimeTableV2Fixture;
import in.koreatech.koin.acceptance.fixture.UserFixture;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TimetableApiTest extends AcceptanceTest {

    @Autowired
    private TimeTableV2Fixture timetableV2Fixture;

    @Autowired
    private TimetableLectureRepositoryV2 timetableLectureRepositoryV2;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private LectureFixture lectureFixture;

    @Autowired
    private SemesterFixture semesterFixture;

    @Autowired
    private DepartmentFixture departmentFixture;

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

    @Test
    void 시간표를_조회한다() throws Exception {
        Department department = departmentFixture.컴퓨터공학부();
        User user = userFixture.준호_학생(department, null).getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester_2019년도_2학기();

        Lecture 건축구조의_이해_및_실습 = lectureFixture.건축구조의_이해_및_실습(semester.getSemester());
        Lecture HRD_개론 = lectureFixture.HRD_개론(semester.getSemester());

        timetableV2Fixture.시간표6(user, semester, 건축구조의_이해_및_실습, HRD_개론, null, null);

        mockMvc.perform(
                get("/timetables")
                    .header("Authorization", "Bearer " + token)
                    .param("semester", semester.getSemester())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "semester": "20192",
                    "timetable": [
                        {
                            "id" : 1,
                            "regular_number": "25",
                            "code": "ARB244",
                            "design_score": "0",
                            "class_time": [200, 201, 202, 203, 204, 205, 206, 207],
                            "class_place": null,
                            "memo": null,
                            "grades": "3",
                            "class_title": "건축구조의 이해 및 실습",
                            "lecture_class": "01",
                            "target": "디자 1 건축",
                            "professor": "황현식",
                            "department": "디자인ㆍ건축공학부"
                        },
                        {
                            "id": 2,
                            "regular_number": "22",
                            "code": "BSM590",
                            "design_score": "0",
                            "class_time": [12, 13, 14, 15, 210, 211, 212, 213],
                            "class_place": null,
                            "memo": null,
                            "grades": "3",
                            "class_title": "컴퓨팅사고",
                            "lecture_class": "06",
                            "target": "기공1",
                            "professor": "박한수,최준호",
                            "department": "기계공학부"
                        }
                    ],
                    "grades": 6,
                    "total_grades": 6
                }
                """));
    }

    @Test
    void 시간표를_조회한다_시간표_프레임_없으면_생성() throws Exception {
        Department department = departmentFixture.컴퓨터공학부();
        User user = userFixture.준호_학생(department, null).getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester_2019년도_2학기();

        mockMvc.perform(
                get("/timetables")
                    .header("Authorization", "Bearer " + token)
                    .param("semester", semester.getSemester())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "semester": "20192",
                    "timetable": [
                    ],
                    "grades": 0,
                    "total_grades": 0
                }
                """));
    }

    @Test
    void 시간표를_생성한다() throws Exception {
        Department department = departmentFixture.컴퓨터공학부();
        User user = userFixture.준호_학생(department, null).getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester_2019년도_2학기();

        lectureFixture.건축구조의_이해_및_실습(semester.getSemester());
        lectureFixture.HRD_개론(semester.getSemester());

       timetableV2Fixture.시간표1(user, semester);

        mockMvc.perform(
                post("/timetables")
                    .header("Authorization", "Bearer " + token)
                    .content("""
                        {
                            "timetable": [
                            {
                               "regular_number": "25",
                               "code": "ARB244",
                               "design_score": "0",
                               "class_time": [200, 201, 202, 203, 204, 205, 206, 207],
                               "class_place": null,
                               "memo": null,
                               "grades": "3",
                               "class_title": "건축구조의 이해 및 실습",
                               "lecture_class": "01",
                               "target": "디자 1 건축",
                               "professor": "황현식",
                               "department": "디자인ㆍ건축공학부"
                            },
                            {
                               "regular_number": "22",
                               "code": "BSM590",
                               "design_score": "0",
                               "class_time": [12, 13, 14, 15, 210, 211, 212, 213],
                               "class_place": null,
                               "memo": null,
                               "grades": "3",
                               "class_title": "컴퓨팅사고",
                               "lecture_class": "06",
                               "target": "기공1",
                               "professor": "박한수,최준호",
                               "department": "기계공학부"
                            }
                            ],
                            "semester": "20192"
                        }
                        """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "semester": "20192",
                    "timetable": [
                        {
                            "id": 1,
                            "regular_number": "25",
                            "code": "ARB244",
                            "design_score": "0",
                            "class_time": [200, 201, 202, 203, 204, 205, 206, 207],
                            "class_place": null,
                            "memo": null,
                            "grades": "3",
                            "class_title": "건축구조의 이해 및 실습",
                            "lecture_class": "01",
                            "target": "디자 1 건축",
                            "professor": "황현식",
                            "department": "디자인ㆍ건축공학부"
                        },
                        {
                            "id": 2,
                            "regular_number": "22",
                            "code": "BSM590",
                            "design_score": "0",
                            "class_time": [12, 13, 14, 15, 210, 211, 212, 213],
                            "class_place": null,
                            "memo": null,
                            "grades": "3",
                            "class_title": "컴퓨팅사고",
                            "lecture_class": "06",
                            "target": "기공1",
                            "professor": "박한수,최준호",
                            "department": "기계공학부"
                        }
                    ],
                    "grades": 6,
                    "total_grades": 6
                }
                """));
    }

    @Test
    void 시간표를_단일_생성한다_전체_반환() throws Exception {
        Department department = departmentFixture.컴퓨터공학부();
        User user = userFixture.준호_학생(department, null).getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester_2019년도_2학기();

        lectureFixture.건축구조의_이해_및_실습(semester.getSemester());
        lectureFixture.HRD_개론(semester.getSemester());

        timetableV2Fixture.시간표1(user, semester);

        mockMvc.perform(
                post("/timetables")
                    .header("Authorization", "Bearer " + token)
                    .content("""
                        {
                          "timetable": [
                           {
                                "regular_number": "25",
                                "code": "ARB244",
                                "design_score": "0",
                                "class_time": [200, 201, 202, 203, 204, 205, 206, 207],
                                "class_place": null,
                                "memo": null,
                                "grades": "3",
                                "class_title": "건축구조의 이해 및 실습",
                                "lecture_class": "01",
                                "target": "디자 1 건축",
                                "professor": "황현식",
                                "department": "디자인ㆍ건축공학부"
                           }
                         ],
                          "semester": "20192"
                        }
                     """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        mockMvc.perform(
                post("/timetables")
                    .header("Authorization", "Bearer " + token)
                    .content("""
                        {
                          "timetable": [
                           {
                                "regular_number": "22",
                                "code": "BSM590",
                                "design_score": "0",
                                "class_time": [12, 13, 14, 15, 210, 211, 212, 213],
                                "class_place": null,
                                "memo": null,
                                "grades": "3",
                                "class_title": "컴퓨팅사고",
                                "lecture_class": "06",
                                "target": "기공1",
                                "professor": "박한수,최준호",
                                "department": "기계공학부"
                           }
                         ],
                          "semester": "20192"
                        }
                     """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "semester": "20192",
                    "timetable": [
                        {
                            "id": 1,
                            "regular_number": "25",
                            "code": "ARB244",
                            "design_score": "0",
                            "class_time": [200, 201, 202, 203, 204, 205, 206, 207],
                            "class_place": null,
                            "memo": null,
                            "grades": "3",
                            "class_title": "건축구조의 이해 및 실습",
                            "lecture_class": "01",
                            "target": "디자 1 건축",
                            "professor": "황현식",
                            "department": "디자인ㆍ건축공학부"
                        },
                        {
                            "id": 2,
                            "regular_number": "22",
                            "code": "BSM590",
                            "design_score": "0",
                            "class_time": [12, 13, 14, 15, 210, 211, 212, 213],
                            "class_place": null,
                            "memo": null,
                            "grades": "3",
                            "class_title": "컴퓨팅사고",
                            "lecture_class": "06",
                            "target": "기공1",
                            "professor": "박한수,최준호",
                            "department": "기계공학부"
                        }
                    ],
                    "grades": 6,
                    "total_grades": 6
                }
                """));
    }

    @Test
    void 시간표를_삭제한다() throws Exception {
        Department department = departmentFixture.컴퓨터공학부();
        User user = userFixture.준호_학생(department, null).getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester_2019년도_2학기();

        Lecture 건축구조의_이해_및_실습 = lectureFixture.건축구조의_이해_및_실습(semester.getSemester());
        Lecture HRD_개론 = lectureFixture.HRD_개론(semester.getSemester());

        timetableV2Fixture.시간표6(user, semester, 건축구조의_이해_및_실습, HRD_개론, null, null);

        mockMvc.perform(
                delete("/timetable")
                    .header("Authorization", "Bearer " + token)
                    .param("id", "2")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();
        assertThat(timetableLectureRepositoryV2.findById(2)).isNotPresent();
    }

/*    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 시간표_삭제_동시성_예외_적절하게_처리하는지_테스트한다() throws InterruptedException {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");

        Lecture 건축구조의_이해_및_실습 = lectureFixture.건축구조의_이해_및_실습(semester.getSemester());
        Lecture HRD_개론 = lectureFixture.HRD_개론(semester.getSemester());

        timetableV2Fixture.시간표6(user, semester, 건축구조의_이해_및_실습, HRD_개론);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        List<Response> responseList = new ArrayList<>();
        Runnable deleteTask = () -> {
            Response response = RestAssured
                    .given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .param("id", 2)
                    .delete("/timetable");
            responseList.add(response);
            latch.countDown();
        };

        executor.submit(deleteTask);
        executor.submit(deleteTask);

        latch.await();

        boolean hasConflict = responseList.stream()
                .anyMatch(response -> response.getStatusCode() == 409);

        assertThat(hasConflict).isTrue();
        assertThat(timetableRepository.findById(2)).isNotPresent();

        executor.shutdown();

    }*/
}
