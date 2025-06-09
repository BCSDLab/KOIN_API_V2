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
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.acceptance.fixture.DepartmentFixture;
import in.koreatech.koin.acceptance.fixture.SemesterFixture;
import in.koreatech.koin.acceptance.fixture.TimeTableV2Fixture;
import in.koreatech.koin.acceptance.fixture.UserFixture;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TimetableFrameApiV3Test extends AcceptanceTest {
    @Autowired
    private TimeTableV2Fixture timetableV2Fixture;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private SemesterFixture semesterFixture;

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
                post("/v3/timetables/frame")
                    .header("Authorization", "Bearer " + token)
                    .content(String.format("""
                        {
                            "year": "%d",
                            "term": "%s"
                        }
                        """, semester.getYear(), semester.getTerm().getDescription()
                    ))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [
                    {
                        "id": 1,
                        "name": "시간표1",
                        "is_main": true
                    }
                ]
                """));
    }

    @Test
    void 특정_시간표_frame을_수정한다() throws Exception {
        TimetableFrame frame = timetableV2Fixture.시간표1(user, semester);
        Integer frameId = frame.getId();

        mockMvc.perform(
                put("/v3/timetables/frame/{id}", frameId)
                    .header("Authorization", "Bearer " + token)
                    .content("""
                        {
                            "name": "새로운 이름",
                            "is_main": true
                        }
                        """
                    )
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [
                    {
                        "id": 1,
                        "name": "새로운 이름",
                        "is_main": true
                    }
                ]
                """));
    }

    @Test
    void 특정_학기의_모든_시간표_frame을_조회한다() throws Exception {
        timetableV2Fixture.시간표1(user, semester);
        timetableV2Fixture.시간표2(user, semester);

        mockMvc.perform(
                get("/v3/timetables/frame")
                    .header("Authorization", "Bearer " + token)
                    .param("year", String.valueOf(semester.getYear()))
                    .param("term", String.valueOf(semester.getTerm().getDescription()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [
                    {
                        "id": 1,
                        "name": "시간표1",
                        "is_main": true
                    },
                    {
                        "id": 2,
                        "name": "시간표2",
                        "is_main": false
                    }
                ]
                """));
    }

    @Test
    void 모든_시간표_프레임을_삭제한다() throws Exception {
        TimetableFrame frame1 = timetableV2Fixture.시간표1(user, semester);
        TimetableFrame frame2 = timetableV2Fixture.시간표1(user, semester);
        TimetableFrame frame3 = timetableV2Fixture.시간표1(user, semester);

        mockMvc.perform(
                delete("/v3/timetables/frames")
                    .header("Authorization", "Bearer " + token)
                    .param("year", String.valueOf(semester.getYear()))
                    .param("term", String.valueOf(semester.getTerm().getDescription()))
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
                get("/v3/timetables/frames")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [
                   {
                     "year": 2024,
                     "timetable_frames": [
                       {
                         "term": "2학기",
                         "frames": [
                           {
                             "id": 3,
                             "name": "시간표1",
                             "is_main": true
                           },
                           {
                             "id": 4,
                             "name": "시간표2",
                             "is_main": false
                           },
                           {
                             "id": 5,
                             "name": "시간표3",
                             "is_main": false
                           }
                         ]
                       },
                       {
                         "term": "1학기",
                         "frames": [
                           {
                             "id": 1,
                             "name": "시간표1",
                             "is_main": true
                           },
                           {
                             "id": 2,
                             "name": "시간표2",
                             "is_main": false
                           }
                         ]
                       }
                     ]
                   }
                 ]
                """));
    }
}
