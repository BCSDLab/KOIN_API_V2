package in.koreatech.koin.acceptance;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.fixture.LectureFixture;
import in.koreatech.koin.fixture.SemesterFixture;
import in.koreatech.koin.fixture.TimeTableV2Fixture;
import in.koreatech.koin.fixture.UserFixture;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TimetableLectureV3ApiTest extends AcceptanceTest {

    @Autowired
    private TimeTableV2Fixture timetableV2Fixture;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private SemesterFixture semesterFixture;

    @Autowired
    private LectureFixture lectureFixture;

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
    void 정규강의를_생성한다() throws Exception {
        timetableV2Fixture.시간표1(user, semester);
        lectureFixture.HRD_개론(semester.getSemester());

        mockMvc.perform(
                post("/v3/timetables/lecture/regular")
                    .header("Authorization", "Bearer " + token)
                    .content("""
                        {
                            "timetable_frame_id" : 1,
                            "lecture_id" : 1
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
                                "week": 0,
                                "start_time": 12,
                                "end_time": 15
                              },
                              {
                                "week": 2,
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
                            "lecture_id" : null,
                            "regular_number": null,
                            "code": null,
                            "design_score": null,
                            "lecture_infos": [
                              {
                                "week": 1,
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
}
