package in.koreatech.koin.acceptance.domain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Collectors;

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
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
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
public class TimetableLectureV3ApiTest extends AcceptanceTest {

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
    private CourseType courseType;
    private Department department;

    @BeforeAll
    void setup() {
        clear();
        department = departmentFixture.컴퓨터공학부();
        user = userFixture.준호_학생(department, null).getUser();
        token = userFixture.getToken(user);
        semester = semesterFixture.semester_2019년도_2학기();
        courseType = courseTypeFixture.이수_구분_선택();
    }

    @Test
    void 정규강의를_생성한다() throws Exception {
        timetableV2Fixture.시간표1(user, semester);
        lectureFixture.HRD_개론(semester.getSemester());

        mockMvc.perform(
                post("/v3/timetables/lecture/regular")
                    .header("Authorization", "Bearer " + token)
                    .content("""
                        {
                            "timetable_frame_id": 1,
                            "lecture_id": 1
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
                            "lecture_id" : 1,
                            "regular_number": "22",
                            "code": "BSM590",
                            "design_score": "0",
                            "lecture_infos": [
                              {
                                "day": 0,
                                "start_time": 12,
                                "end_time": 15,
                                "place": ""
                              },
                              {
                                "day": 2,
                                "start_time": 210,
                                "end_time": 213,
                                "place": ""
                              }
                            ],
                            "memo": null,
                            "grades": "3",
                            "class_title": "컴퓨팅사고",
                            "lecture_class": "06",
                            "target": "기공1",
                            "professor": "박한수,최준호",
                            "department": "기계공학부"
                        }
                    ],
                    "grades": 3,
                    "total_grades": 3
                }
                """));
    }

    @Test
    void 커스텀강의를_생성한다() throws Exception {
        timetableV2Fixture.시간표1(user, semester);

        mockMvc.perform(
                post("/v3/timetables/lecture/custom")
                    .header("Authorization", "Bearer " + token)
                    .content("""
                        {
                          "timetable_frame_id": 1,
                          "timetable_lecture": {
                            "class_title": "커스텀 강의 이름",
                            "lecture_infos": [
                              {
                                "start_time": 112,
                                "end_time": 115,
                                "place": "2공학관314"
                              }
                            ],
                            "professor": "교수명",
                            "grades": "3",
                            "memo": "메모"
                          }
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
                            "lecture_id": null,
                            "regular_number": null,
                            "code": null,
                            "design_score": null,
                            "lecture_infos": [
                              {
                                "day": 1,
                                "start_time": 112,
                                "end_time": 115,
                                "place": "2공학관314"
                              }
                            ],
                            "memo": "메모",
                            "grades": "3",
                            "class_title": "커스텀 강의 이름",
                            "lecture_class": null,
                            "target": null,
                            "professor": "교수명",
                            "department": null
                        }
                    ],
                    "grades": 3,
                    "total_grades": 3
                }
                """));
    }

    @Test
    void 시간표에서_삭제된_강의를_복구한다_V3() throws Exception {
        Lecture 건축구조의_이해_및_실습 = lectureFixture.건축구조의_이해_및_실습(semester.getSemester());
        Lecture HRD_개론 = lectureFixture.HRD_개론(semester.getSemester());
        CourseType HRD_필수 = courseTypeFixture.HRD_필수();
        CourseType 전공_필수 = courseTypeFixture.전공_필수();
        TimetableFrame frame = timetableV2Fixture.시간표8(user, semester, 건축구조의_이해_및_실습, HRD_개론, 전공_필수, HRD_필수);

        List<Integer> timetableLecturesId = frame.getTimetableLectures().stream()
            .map(TimetableLecture::getId)
            .toList();

        mockMvc.perform(
                post("/v3/timetables/lecture/rollback")
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
                            "lecture_infos": [
                              {
                                "day": 2,
                                "start_time": 200,
                                "end_time": 207,
                                "place": ""
                              }
                            ],
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
                            "lecture_infos": [
                              {
                                "day": 0,
                                "start_time": 12,
                                "end_time": 15
                              },
                              {
                                "day": 2,
                                "start_time": 210,
                                "end_time": 213,
                                "place": ""
                              }
                            ],
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
    void 삭제된_시간표프레임과_그에_해당하는_강의를_복구한다_V3() throws Exception {
        Lecture 건축구조의_이해_및_실습 = lectureFixture.건축구조의_이해_및_실습(semester.getSemester());
        Lecture HRD_개론 = lectureFixture.HRD_개론(semester.getSemester());
        CourseType HRD_필수 = courseTypeFixture.HRD_필수();
        CourseType 전공_필수 = courseTypeFixture.전공_필수();
        TimetableFrame frame = timetableV2Fixture.시간표7(user, semester, 건축구조의_이해_및_실습, HRD_개론, 전공_필수, HRD_필수);

        mockMvc.perform(
                post("/v3/timetables/frame/rollback")
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
                            "lecture_infos": [
                              {
                                "day": 2,
                                "start_time": 200,
                                "end_time": 207,
                                "place": ""
                              }
                            ],
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
                            "lecture_infos": [
                              {
                                "day": 0,
                                "start_time": 12,
                                "end_time": 15
                              },
                              {
                                "day": 2,
                                "start_time": 210,
                                "end_time": 213,
                                "place": ""
                              }
                            ],
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
}
