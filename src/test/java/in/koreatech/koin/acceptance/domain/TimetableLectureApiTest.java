package in.koreatech.koin.acceptance.domain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.CourseTypeAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.LectureAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.SemesterAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.TimeTableV2AcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.domain.graduation.model.CourseType;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.user.model.User;

public class TimetableLectureApiTest extends AcceptanceTest {

    @Autowired
    private TimeTableV2AcceptanceFixture timetableV2Fixture;

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private SemesterAcceptanceFixture semesterFixture;

    @Autowired
    private LectureAcceptanceFixture lectureFixture;

    @Autowired
    private CourseTypeAcceptanceFixture courseTypeFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    private User user;
    private String token;
    private Semester semester;
    private Department department;

    @BeforeAll
    void setup() {
        clear();
        department = departmentFixture.컴퓨터공학부();
        user = userFixture.준호_학생(department, null).getUser();
        token = userFixture.getToken(user);
        semester = semesterFixture.semester_2019년도_2학기();
    }

    @Test
    void 시간표를_생성한다_TimetableLecture() throws Exception {
        timetableV2Fixture.시간표1(user, semester);

        mockMvc.perform(
                post("/v2/timetables/lecture")
                    .header("Authorization", "Bearer " + token)
                    .content("""
                        {
                            "timetable_frame_id" : 1,
                            "timetable_lecture": [
                                {
                                    "class_title": "커스텀생성1",
                                    "class_infos": [
                                        {
                                            "class_time" : [200, 201],
                                            "class_place" : "한기대"
                                        }
                                    ],
                                    "professor" : "서정빈",
                                    "grades": "2",
                                    "memo" : "메모"
                                },
                                {
                                    "class_title": "커스텀생성2",
                                    "class_infos": [
                                        {
                                            "class_time" : [202, 203],
                                            "class_place" : "참빛관 편의점"
                                        }
                                    ],
                                    "professor" : "감사 서정빈",
                                    "grades": "1",
                                    "memo" : "메모"
                                }
                            ]
                        }
                        """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "timetable_frame_id": 1,
                    "timetable": [
                        {
                            "id": 1,
                            "lecture_id" : null,
                            "regular_number": null,
                            "code": null,
                            "design_score": null,
                            "class_infos": [
                              {
                                "class_time": [200, 201],
                                "class_place": "한기대"
                              }
                            ],
                            "memo": "메모",
                            "grades": "2",
                            "class_title": "커스텀생성1",
                            "lecture_class": null,
                            "target": null,
                            "professor": "서정빈",
                            "department": null,
                            "course_type": null
                        },
                        {
                            "id": 2,
                            "lecture_id" : null,
                            "regular_number": null,
                            "code": null,
                            "design_score": null,
                            "class_infos": [
                              {
                                "class_time": [202, 203],
                                "class_place": "참빛관 편의점"
                              }
                            ],
                            "memo": "메모",
                            "grades": "1",
                            "class_title": "커스텀생성2",
                            "lecture_class": null,
                            "target": null,
                            "professor": "감사 서정빈",
                            "department": null,
                            "course_type": null
                        }
                    ],
                    "grades": 3,
                    "total_grades": 3
                }
                """));
    }

    @Test
    void 시간표를_수정한다_TimetableLecture() throws Exception {
        timetableV2Fixture.시간표3(user, semester);

        mockMvc.perform(
                put("/v2/timetables/lecture")
                    .header("Authorization", "Bearer " + token)
                    .content("""
                        {
                            "timetable_frame_id" : 1,
                            "timetable_lecture": [
                                {
                                    "id": 1,
                                    "class_title": "커스텀바꿔요1",
                                    "class_infos": [
                                        {
                                            "class_time" : [200, 201],
                                            "class_place" : "한기대"
                                        }
                                    ],
                                    "professor" : "서정빈",
                                    "grades" : "0",
                                    "memo" : "메모한당 히히"
                                },
                                {
                                    "id": 2,
                                    "class_title": "커스텀바꿔요2",
                                    "class_infos": [
                                        {
                                            "class_time" : [202, 203],
                                            "class_place" : "참빛관 편의점"
                                        }
                                    ],
                                    "professor" : "알바 서정빈",
                                    "grades" : "0",
                                    "memo" : "메모한당 히히"
                                }
                            ]
                        }
                        """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "timetable_frame_id": 1,
                    "timetable": [
                        {
                            "id": 1,
                            "lecture_id" : null,
                            "regular_number": null,
                            "code": null,
                            "design_score": null,
                            "class_infos": [
                              {
                                "class_time": [200, 201],
                                "class_place": "한기대"
                              }
                            ],
                            "memo": "메모한당 히히",
                            "grades": "0",
                            "class_title": "커스텀바꿔요1",
                            "lecture_class": null,
                            "target": null,
                            "professor": "서정빈",
                            "department": null,
                            "course_type": null
                        },
                        {
                            "id": 2,
                            "lecture_id" : null,
                            "regular_number": null,
                            "code": null,
                            "design_score": null,
                            "class_infos": [
                              {
                                "class_time": [202, 203],
                                "class_place": "참빛관 편의점"
                              }
                            ],
                            "memo": "메모한당 히히",
                            "grades": "0",
                            "class_title": "커스텀바꿔요2",
                            "lecture_class": null,
                            "target": null,
                            "professor": "알바 서정빈",
                            "department": null,
                            "course_type": null
                        }
                    ],
                    "grades": 0,
                    "total_grades": 0
                }
                """));
    }

    @Test
    void 시간표를_조회한다_TimetableLecture() throws Exception {
        Lecture 건축구조의_이해_및_실습 = lectureFixture.건축구조의_이해_및_실습(semester.getSemester());
        Lecture HRD_개론 = lectureFixture.HRD_개론(semester.getSemester());

        CourseType 전공_필수 = courseTypeFixture.전공_필수();
        CourseType HRD_필수 = courseTypeFixture.HRD_필수();

        TimetableFrame frame = timetableV2Fixture.시간표6(user, semester, 건축구조의_이해_및_실습, HRD_개론, 전공_필수, HRD_필수);

        mockMvc.perform(
                get("/v2/timetables/lecture")
                    .header("Authorization", "Bearer " + token)
                    .param("timetable_frame_id", String.valueOf(frame.getId()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "timetable_frame_id": 1,
                    "timetable": [
                        {
                            "id" : 1,
                            "lecture_id" : 1,
                            "regular_number": "25",
                            "code": "ARB244",
                            "design_score": "0",
                            "class_infos": [
                              {
                                "class_time": [200, 201, 202, 203, 204, 205, 206, 207],
                                "class_place": null
                              }
                            ],
                            "memo": null,
                            "grades": "3",
                            "class_title": "건축구조의 이해 및 실습",
                            "lecture_class": "01",
                            "target": "디자 1 건축",
                            "professor": "황현식",
                            "department": "디자인ㆍ건축공학부",
                            "course_type": "전공 필수"
                        },
                        {
                            "id": 2,
                            "lecture_id": 2,
                            "regular_number": "22",
                            "code": "BSM590",
                            "design_score": "0",
                            "class_infos": [
                              {
                                "class_time": [12, 13, 14, 15, 210, 211, 212, 213],
                                "class_place": null
                              }
                            ],
                            "memo": null,
                            "grades": "3",
                            "class_title": "컴퓨팅사고",
                            "lecture_class": "06",
                            "target": "기공1",
                            "professor": "박한수,최준호",
                            "department": "기계공학부",
                            "course_type": "HRD 필수"
                        }
                    ],
                    "grades": 6,
                    "total_grades": 6
                }
                """));
    }

    @Test
    void 시간표에서_특정_강의를_삭제한다() throws Exception {
        Lecture lecture1 = lectureFixture.HRD_개론("20192");
        Lecture lecture2 = lectureFixture.영어청해("20192");
        CourseType courseType1 = courseTypeFixture.HRD_필수();
        CourseType courseType2 = courseTypeFixture.교양_필수();

        TimetableFrame frame = timetableV2Fixture.시간표4(user, semester, lecture1, lecture2, courseType1, courseType2);

        Integer lectureId = lecture1.getId();

        mockMvc.perform(
                delete("/v2/timetables/lecture/{id}", lectureId)
                    .header("Authorization", "Bearer " + token)
                    .param("timetable_frame_id", String.valueOf(frame.getId()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());
    }

    @Test
    void 시간표에서_특정_강의를_삭제한다_V2() throws Exception {
        Lecture lecture1 = lectureFixture.HRD_개론("20192");
        Lecture lecture2 = lectureFixture.영어청해("20192");
        CourseType courseType1 = courseTypeFixture.HRD_필수();
        CourseType courseType2 = courseTypeFixture.교양_필수();

        TimetableFrame frame = timetableV2Fixture.시간표4(user, semester, lecture1, lecture2, courseType1, courseType2);

        Integer frameId = frame.getId();
        Integer lectureId = lecture1.getId();

        mockMvc.perform(
                delete("/v2/timetables/frame/{frameId}/lecture/{lectureId}", frameId, lectureId)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());
    }

    @Test
    void 시간표에서_여러개의_강의를_한번에_삭제한다_V2() throws Exception {
        Lecture lecture1 = lectureFixture.HRD_개론("20192");
        Lecture lecture2 = lectureFixture.영어청해("20192");
        CourseType courseType1 = courseTypeFixture.HRD_필수();
        CourseType courseType2 = courseTypeFixture.교양_필수();

        TimetableFrame frame = timetableV2Fixture.시간표4(user, semester, lecture1, lecture2, courseType1, courseType2);

        List<Integer> timetableLectureIds = frame.getTimetableLectures().stream()
            .map(TimetableLecture::getId)
            .toList();

        mockMvc.perform(
                delete("/v2/timetables/lectures")
                    .header("Authorization", "Bearer " + token)
                    .param("timetable_lecture_ids", timetableLectureIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(",")))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());
    }

    @Test
    void 시간표에서_삭제된_강의를_복구한다_V2() throws Exception {
        Lecture 건축구조의_이해_및_실습 = lectureFixture.건축구조의_이해_및_실습(semester.getSemester());
        Lecture HRD_개론 = lectureFixture.HRD_개론(semester.getSemester());
        CourseType courseType1 = courseTypeFixture.전공_필수();
        CourseType courseType2 = courseTypeFixture.교양_필수();
        TimetableFrame frame = timetableV2Fixture.시간표8(user, semester, 건축구조의_이해_및_실습, HRD_개론, courseType1, courseType2);

        List<Integer> timetableLecturesId = frame.getTimetableLectures().stream()
            .map(TimetableLecture::getId)
            .toList();

        mockMvc.perform(
                post("/v2/timetables/lecture/rollback")
                    .header("Authorization", "Bearer " + token)
                    .param("timetable_lectures_id", timetableLecturesId.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(",")))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "timetable_frame_id": 1,
                    "timetable": [
                        {
                            "id" : 1,
                            "lecture_id" : 1,
                            "regular_number": "25",
                            "code": "ARB244",
                            "design_score": "0",
                            "class_infos": [
                              {
                                "class_time": [200, 201, 202, 203, 204, 205, 206, 207],
                                "class_place": null
                              }
                            ],
                            "memo": null,
                            "grades": "3",
                            "class_title": "건축구조의 이해 및 실습",
                            "lecture_class": "01",
                            "target": "디자 1 건축",
                            "professor": "황현식",
                            "department": "디자인ㆍ건축공학부",
                            "course_type": "전공 필수"
                        },
                        {
                            "id": 2,
                            "lecture_id": 2,
                            "regular_number": "22",
                            "code": "BSM590",
                            "design_score": "0",
                            "class_infos": [
                              {
                                "class_time": [12, 13, 14, 15, 210, 211, 212, 213],
                                "class_place": null
                              }
                            ],
                            "memo": null,
                            "grades": "3",
                            "class_title": "컴퓨팅사고",
                            "lecture_class": "06",
                            "target": "기공1",
                            "professor": "박한수,최준호",
                            "department": "기계공학부",
                            "course_type": "교양 필수"
                        }
                    ],
                    "grades": 6,
                    "total_grades": 6
                }
                """));
    }

    @Test
    void 삭제된_시간표프레임과_그에_해당하는_강의를_복구한다_V2() throws Exception {
        Lecture 건축구조의_이해_및_실습 = lectureFixture.건축구조의_이해_및_실습(semester.getSemester());
        Lecture HRD_개론 = lectureFixture.HRD_개론(semester.getSemester());
        CourseType courseType1 = courseTypeFixture.전공_필수();
        CourseType courseType2 = courseTypeFixture.교양_필수();
        TimetableFrame frame = timetableV2Fixture.시간표7(user, semester, 건축구조의_이해_및_실습, HRD_개론, courseType1, courseType2);

        mockMvc.perform(
                post("/v2/timetables/frame/rollback")
                    .header("Authorization", "Bearer " + token)
                    .param("timetable_frame_id", String.valueOf(frame.getId()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "timetable_frame_id": 1,
                    "timetable": [
                        {
                            "id" : 1,
                            "lecture_id" : 1,
                            "regular_number": "25",
                            "code": "ARB244",
                            "design_score": "0",
                            "class_infos": [
                              {
                                "class_time": [200, 201, 202, 203, 204, 205, 206, 207],
                                "class_place": null
                              }
                            ],
                            "memo": null,
                            "grades": "3",
                            "class_title": "건축구조의 이해 및 실습",
                            "lecture_class": "01",
                            "target": "디자 1 건축",
                            "professor": "황현식",
                            "department": "디자인ㆍ건축공학부",
                            "course_type": "전공 필수"
                        },
                        {
                            "id": 2,
                            "lecture_id": 2,
                            "regular_number": "22",
                            "code": "BSM590",
                            "design_score": "0",
                            "class_infos": [
                              {
                                "class_time": [12, 13, 14, 15, 210, 211, 212, 213],
                                "class_place": null
                              }
                            ],
                            "memo": null,
                            "grades": "3",
                            "class_title": "컴퓨팅사고",
                            "lecture_class": "06",
                            "target": "기공1",
                            "professor": "박한수,최준호",
                            "department": "기계공학부",
                            "course_type": "교양 필수"
                        }
                    ],
                    "grades": 6,
                    "total_grades": 6
                }
                """));
    }

    /*@Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void isMain이_false인_frame과_true인_frame을_동시에_삭제한다() {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");

        TimetableFrame mainFrame = timetableV2Fixture.시간표1(user, semester);
        TimetableFrame frame1 = timetableV2Fixture.시간표2(user, semester);
        TimetableFrame frame2 = timetableV2Fixture.시간표3(user, semester);

        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        executorService.execute(() -> {
            try {
                RestAssured
                    .given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .param("id", frame1.getId())
                    .delete("/v2/timetables/frame")
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
            } finally {
                latch.countDown();
            }
        });

        executorService.execute(() -> {
            try {
                RestAssured
                    .given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .param("id", mainFrame.getId())
                    .delete("/v2/timetables/frame")
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executorService.shutdown();
        }

        TimetableFrame reloadedFrame2 = timetableFrameRepositoryV2.findById(frame2.getId()).orElseThrow();
        assertThat(reloadedFrame2.isMain()).isTrue();
    }*/
}
