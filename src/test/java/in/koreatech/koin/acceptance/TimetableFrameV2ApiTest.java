package in.koreatech.koin.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

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
@TestInstance(Lifecycle.PER_CLASS)
public class TimetableFrameV2ApiTest {
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

    private User user;
    private String token;
    private Semester semester;

    @BeforeAll
    void setup() {
        clear();
        user = userFixture.준호_학생().getUser();
        token = userFixture.getToken(user);
        semester = semesterFixture.semester("20192");
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

        assertThat(timetableFrameRepositoryV2.findById(frame1.getId())).isNotPresent();
        assertThat(timetableFrameRepositoryV2.findById(frame2.getId())).isNotPresent();
        assertThat(timetableFrameRepositoryV2.findById(frame3.getId())).isNotPresent();
    }
}
