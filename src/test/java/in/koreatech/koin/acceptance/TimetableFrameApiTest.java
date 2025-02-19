package in.koreatech.koin.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.graduation.model.CourseType;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableLectureRepositoryV2;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.fixture.CourseTypeFixture;
import in.koreatech.koin.fixture.DepartmentFixture;
import in.koreatech.koin.fixture.LectureFixture;
import in.koreatech.koin.fixture.SemesterFixture;
import in.koreatech.koin.fixture.TimeTableV2Fixture;
import in.koreatech.koin.fixture.UserFixture;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(Lifecycle.PER_CLASS)
public class TimetableFrameApiTest extends AcceptanceTest {

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

    @Autowired
    private CourseTypeFixture courseTypeFixture;

    @Autowired
    private DepartmentFixture departmentFixture;

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
    void 특정_시간표_frame을_생성한다() throws Exception {
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
        TimetableFrame frame = timetableV2Fixture.시간표1(user, semester);
        Integer frameId = frame.getId();

        mockMvc.perform(
                put("/v2/timetables/frame/{id}", frameId)
                    .header("Authorization", "Bearer " + token)
                    .content("""
                        {
                            "timetable_name": "새로운 이름",
                            "is_main": true
                        }
                        """
                    )
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
        Lecture lecture = lectureFixture.HRD_개론(semester.getSemester());
        CourseType courseType = courseTypeFixture.HRD_필수();
        TimetableFrame frame1 = timetableV2Fixture.시간표5(user, semester, lecture, courseType);

        mockMvc.perform(
                delete("/v2/timetables/frame")
                    .header("Authorization", "Bearer " + token)
                    .param("id", String.valueOf(frame1.getId()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());

        assertThat(frame1.isDeleted()).isTrue();
        assertThat(frame1.getTimetableLectures().get(1).isDeleted()).isTrue();
    }

    @Test
    void isMain인_frame을_삭제한다_다른_frame이_main으로_됨() throws Exception {
        TimetableFrame frame1 = timetableV2Fixture.시간표1(user, semester);
        TimetableFrame frame2 = timetableV2Fixture.시간표2(user, semester);

        mockMvc.perform(
                delete("/v2/timetables/frame")
                    .header("Authorization", "Bearer " + token)
                    .param("id", String.valueOf(frame1.getId()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());

        assertThat(frame1.isDeleted()).isTrue();

        TimetableFrame reloadedFrame2 = timetableFrameRepositoryV2.findById(frame2.getId()).orElseThrow();
        assertThat(reloadedFrame2.isMain()).isTrue();
    }

    @Test
    void 특정_시간표_frame을_삭제한다_본인_삭제가_아니면_403_반환() throws Exception {
        Department department1 = departmentFixture.컴퓨터공학부();
        User user1 = userFixture.성빈_학생(department1).getUser();
        String token = userFixture.getToken(user1);

        TimetableFrame frame1 = timetableV2Fixture.시간표1(user, semester);

        mockMvc.perform(
                delete("/v2/timetables/frame")
                    .header("Authorization", "Bearer " + token)
                    .param("id", String.valueOf(frame1.getId()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void 모든_시간표_프레임을_삭제한다() throws Exception {
        TimetableFrame frame1 = timetableV2Fixture.시간표1(user, semester);
        TimetableFrame frame2 = timetableV2Fixture.시간표1(user, semester);
        TimetableFrame frame3 = timetableV2Fixture.시간표1(user, semester);

        mockMvc.perform(
                delete("/v2/all/timetables/frame")
                    .header("Authorization", "Bearer " + token)
                    .param("semester", semester.getSemester())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());

        assertThat(frame1.isDeleted()).isTrue();
        assertThat(frame2.isDeleted()).isTrue();
        assertThat(frame3.isDeleted()).isTrue();
    }

    @Test
    void 모든_학기의_시간표_프레임을_조회한다() throws Exception {
        Semester semester1 = semesterFixture.semester_2024년도_1학기();
        Semester semester2 = semesterFixture.semester_2024년도_2학기();

        timetableV2Fixture.시간표1(user, semester1);
        timetableV2Fixture.시간표2(user, semester1);

        timetableV2Fixture.시간표1(user, semester2);
        timetableV2Fixture.시간표2(user, semester2);
        timetableV2Fixture.시간표3(user, semester2);

        mockMvc.perform(
                get("/v2/timetables/frames")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
            {
                "semesters": {
                    "20241": [
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
                    ],
                    "20242": [
                        {
                            "id": 3,
                            "timetable_name": "시간표1",
                            "is_main": true
                        },
                        {
                            "id": 4,
                            "timetable_name": "시간표2",
                            "is_main": false
                        },
                        {
                            "id": 5,
                            "timetable_name": "시간표3",
                            "is_main": false
                        }
                    ]
                }
            }
            """));
    }
}
