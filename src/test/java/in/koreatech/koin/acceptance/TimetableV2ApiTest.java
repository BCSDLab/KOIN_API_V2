package in.koreatech.koin.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableLectureRepositoryV2;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.fixture.LectureFixture;
import in.koreatech.koin.fixture.SemesterFixture;
import in.koreatech.koin.fixture.TimeTableV2Fixture;
import in.koreatech.koin.fixture.UserFixture;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TimetableV2ApiTest extends AcceptanceTest {

    @Autowired
    private TimeTableV2Fixture timetableV2Fixture;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private SemesterFixture semesterFixture;

    @Autowired
    private LectureFixture lectureFixture;

    @Autowired
    private TimetableFrameRepositoryV2 timetableFrameRepositoryV2;

    @Autowired
    private TimetableLectureRepositoryV2 timetableLectureRepositoryV2;

    @BeforeAll
    void setup() {
        clear();
    }

    @Test
    void 특정_시간표_frame을_생성한다() throws Exception {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");

        mockMvc.perform(
                post("/v2/timetables/frame")
                    .header("Authorization", "Bearer " + token)
                    .content(String.format("""
                        {
                            "semester": "%s"
                        }
                        """, semester.getSemester()
                    ))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "id": 1,
                    "timetable_name": "시간표1",
                    "is_main": true
                }
                """));
    }

    @Test
    void 특정_시간표_frame을_이름을_지어_생성한다() throws Exception {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");

        mockMvc.perform(
                post("/v2/timetables/frame")
                    .header("Authorization", "Bearer " + token)
                    .content(String.format("""
                        {
                            "semester": "%s",
                            "timetable_name": "%s"
                        }
                        """, semester.getSemester(), "이름지어본시간표"
                    ))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "id": 1,
                    "timetable_name": "이름지어본시간표",
                    "is_main": true
                }
                """));
    }

    @Test
    void 특정_시간표_frame을_수정한다() throws Exception {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");
        TimetableFrame frame = timetableV2Fixture.시간표1(user, semester);
        Integer frameId = frame.getId();

        mockMvc.perform(
                put("/v2/timetables/frame/{id}", frameId)
                    .header("Authorization", "Bearer " + token)
                    .content(String.format("""
                        {
                            "timetable_name": "새로운 이름",
                            "is_main": true
                        }
                        """
                    ))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "id": 1,
                    "timetable_name": "새로운 이름",
                    "is_main": true
                }
                """));
    }

    @Test
    void 모든_시간표_frame을_조회한다() throws Exception {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");

        timetableV2Fixture.시간표1(user, semester);
        timetableV2Fixture.시간표2(user, semester);

        mockMvc.perform(
                get("/v2/timetables/frames")
                    .header("Authorization", "Bearer " + token)
                    .param("semester", semester.getSemester())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [
                    {
                        "id": 1,
                        "timetable_name": "시간표1",
                        "is_main": true
                    },
                    {
                        "id": 2,
                        "timetable_name": "시간표2",
                        "is_main": false
                    }
                ]
                """));
    }

    @Test
    void 강의를_담고_있는_특정_시간표_frame을_삭제한다() throws Exception {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");
        Lecture lecture = lectureFixture.HRD_개론(semester.getSemester());

        TimetableFrame frame1 = timetableV2Fixture.시간표5(user, semester, lecture);

        mockMvc.perform(
                delete("/v2/timetables/frame")
                    .header("Authorization", "Bearer " + token)
                    .param("id", String.valueOf(frame1.getId()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());

        assertThat(timetableFrameRepositoryV2.findById(frame1.getId())).isNotPresent();
        assertThat(timetableLectureRepositoryV2.findById(frame1.getTimetableLectures().get(1).getId())).isNotPresent();
    }

    @Test
    void isMain인_frame을_삭제한다_다른_frame이_main으로_됨() throws Exception {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");

        TimetableFrame frame1 = timetableV2Fixture.시간표1(user, semester);
        TimetableFrame frame2 = timetableV2Fixture.시간표2(user, semester);

        mockMvc.perform(
                delete("/v2/timetables/frame")
                    .header("Authorization", "Bearer " + token)
                    .param("id", String.valueOf(frame1.getId()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());

        assertThat(timetableFrameRepositoryV2.findById(frame1.getId())).isNotPresent();

        TimetableFrame reloadedFrame2 = timetableFrameRepositoryV2.findById(frame2.getId()).orElseThrow();
        assertThat(reloadedFrame2.isMain()).isTrue();
    }

    @Test
    void 특정_시간표_frame을_삭제한다_본인_삭제가_아니면_403_반환() throws Exception {
        User user1 = userFixture.준호_학생().getUser();
        User user2 = userFixture.성빈_학생().getUser();
        String token = userFixture.getToken(user2);
        Semester semester = semesterFixture.semester("20192");

        TimetableFrame frame1 = timetableV2Fixture.시간표1(user1, semester);

        mockMvc.perform(
                delete("/v2/timetables/frame")
                    .header("Authorization", "Bearer " + token)
                    .param("id", String.valueOf(frame1.getId()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void 시간표를_생성한다_TimetableLecture() throws Exception {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");
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
                                    "class_time" : [200, 201],
                                    "class_place" : "한기대",
                                    "professor" : "서정빈",
                                    "grades": "2",
                                    "memo" : "메모"
                                },
                                {
                                    "class_title": "커스텀생성2",
                                    "class_time" : [202, 203],
                                    "class_place" : "참빛관 편의점",
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
                        "class_time": [200, 201],
                        "class_place": "한기대",
                        "memo": "메모",
                        "grades": "2",
                        "class_title": "커스텀생성1",
                        "lecture_class": null,
                        "target": null,
                        "professor": "서정빈",
                        "department": null
                    },
                    {
                        "id": 2,
                        "lecture_id" : null,
                        "regular_number": null,
                        "code": null,
                        "design_score": null,
                        "class_time": [202, 203],
                        "class_place": "참빛관 편의점",
                        "memo": "메모",
                        "grades": "1",
                        "class_title": "커스텀생성2",
                        "lecture_class": null,
                        "target": null,
                        "professor": "감사 서정빈",
                        "department": null
                    }
                ],
                "grades": 3,
                "total_grades": 3
            }
            """));
    }

    @Test
    void 시간표를_수정한다_TimetableLecture() throws Exception {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");
        TimetableFrame frame = timetableV2Fixture.시간표3(user, semester);
        Integer frameId = frame.getId();

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
                                    "class_time" : [200, 201],
                                    "class_place" : "한기대",
                                    "professor" : "서정빈",
                                    "grades" : "0",
                                    "memo" : "메모한당 히히"
                                },
                                {
                                    "id": 2,
                                    "class_title": "커스텀바꿔요2",
                                    "class_time" : [202, 203],
                                    "class_place" : "참빛관 편의점",
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
                        "class_time": [200, 201],
                        "class_place": "한기대",
                        "memo": "메모한당 히히",
                        "grades": "0",
                        "class_title": "커스텀바꿔요1",
                        "lecture_class": null,
                        "target": null,
                        "professor": "서정빈",
                        "department": null
                    },
                    {
                        "id": 2,
                        "lecture_id" : null,
                        "regular_number": null,
                        "code": null,
                        "design_score": null,
                        "class_time": [202, 203],
                        "class_place": "참빛관 편의점",
                        "memo": "메모한당 히히",
                        "grades": "0",
                        "class_title": "커스텀바꿔요2",
                        "lecture_class": null,
                        "target": null,
                        "professor": "알바 서정빈",
                        "department": null
                    }
                ],
                "grades": 0,
                "total_grades": 0
            }
            """));
    }

    @Test
    void 시간표를_조회한다_TimetableLecture() throws Exception {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");

        Lecture 건축구조의_이해_및_실습 = lectureFixture.건축구조의_이해_및_실습(semester.getSemester());
        Lecture HRD_개론 = lectureFixture.HRD_개론(semester.getSemester());

        TimetableFrame frame = timetableV2Fixture.시간표6(user, semester, 건축구조의_이해_및_실습, HRD_개론);

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
                        "lecture_id": 2,
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
    void 시간표에서_특정_강의를_삭제한다() throws Exception {
        User user1 = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user1);
        Semester semester = semesterFixture.semester("20192");
        Lecture lecture1 = lectureFixture.HRD_개론("20192");
        Lecture lecture2 = lectureFixture.영어청해("20192");
        TimetableFrame frame = timetableV2Fixture.시간표4(user1, semester, lecture1, lecture2);

        Integer lectureId = lecture1.getId();

        mockMvc.perform(
                delete("/v2/timetables/lecture/{id}", lectureId)
                    .header("Authorization", "Bearer " + token)
                    .param("timetable_frame_id", String.valueOf(frame.getId()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());
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
