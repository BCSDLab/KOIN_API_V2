package in.koreatech.koin.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

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
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SuppressWarnings("NonAsciiCharacters")
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

    @Test
    @DisplayName("특정 시간표 frame을 생성한다")
    void createTimeTablesFrame() {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                  "semester": "%s"
                }
                """, semester.getSemester()
            ))
            .when()
            .post("/v2/timetables/frame")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "id": 1,
                    "timetable_name": "시간표1",
                    "is_main": true
                }
                """);
    }

    @Test
    @DisplayName("특정 시간표 frame을 수정한다")
    void updateTimetableFrame() {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");
        TimetableFrame frame = timetableV2Fixture.시간표1(user, semester);
        Integer frameId = frame.getId();

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                  "name": "새로운 이름",
                  "is_main": true
                }
                """
            ))
            .when()
            .put("/v2/timetables/frame/{id}", frameId)
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "id": 1,
                    "name": "새로운 이름",
                    "is_main": true
                }
                """);
    }

    @Test
    @DisplayName("모든 시간표 frame을 조회한다")
    void getAllTimeTablesFrame() {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");

        timetableV2Fixture.시간표1(user, semester);
        timetableV2Fixture.시간표2(user, semester);

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .param("semester", semester.getSemester())
            .get("/v2/timetables/frames")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
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
                """);
    }

    @Test
    @DisplayName("강의를 담고 있는 특정 시간표 frame을 삭제한다")
    void deleteTimeTablesFrame() {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");
        Lecture lecture = lectureFixture.HRD_개론(semester.getSemester());

        TimetableFrame frame1 = timetableV2Fixture.시간표5(user, semester, lecture);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .param("id", frame1.getId())
            .delete("/v2/timetables/frame")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(timetableFrameRepositoryV2.findById(frame1.getId())).isNotPresent();
        assertThat(timetableLectureRepositoryV2.findById(frame1.getTimetableLectures().get(1).getId())).isNotPresent();
    }

    @Test
    @DisplayName("특정 시간표 frame을 삭제한다 - 본인 삭제가 아니면 403 반환")
    void deleteTimeTablesFrameNoAuth() {
        User user1 = userFixture.준호_학생().getUser();
        User user2 = userFixture.성빈_학생().getUser();
        String token = userFixture.getToken(user2);
        Semester semester = semesterFixture.semester("20192");

        TimetableFrame frame1 = timetableV2Fixture.시간표1(user1, semester);

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .param("id", frame1.getId())
            .delete("/v2/timetables/frame")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("시간표를 생성한다 - TimetableLecture")
    void createTimetableLecture() {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");
        timetableV2Fixture.시간표1(user, semester);

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType("application/json")
            .body("""
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
            .when()
            .post("/v2/timetables/lecture")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
            {
                "timetable_frame_id": 1,
                "timetable": [
                    {
                        "id": 1,
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
            """);
    }

    @Test
    @DisplayName("시간표를 수정한다 - TimetableLecture")
    void updateTimetableLecture() {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");
        TimetableFrame frame = timetableV2Fixture.시간표3(user, semester);
        Integer frameId = frame.getId();

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType("application/json")
            .body("""
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
            .when()
            .put("/v2/timetables/lecture")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
            {
                "timetable_frame_id": 1,
                "timetable": [
                    {
                        "id": 1,
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
            """);
    }

    @Test
    @DisplayName("시간표를 조회한다 - TimetableLecture")
    void getTimetableLecture() {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");

        Lecture 건축구조의_이해_및_실습 = lectureFixture.건축구조의_이해_및_실습(semester.getSemester());
        Lecture HRD_개론 = lectureFixture.HRD_개론(semester.getSemester());

        TimetableFrame frame = timetableV2Fixture.시간표6(user, semester, 건축구조의_이해_및_실습, HRD_개론);

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType("application/json")
            .when()
            .param("timetable_frame_id", frame.getId())
            .get("/v2/timetables/lecture")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
            {
                "timetable_frame_id": 1,
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
            """);
    }

    @Test
    @DisplayName("시간표에서 특정 강의를 삭제한다")
    void deleteTimetableLecture() {
        User user1 = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user1);
        Semester semester = semesterFixture.semester("20192");
        Lecture lecture1 = lectureFixture.HRD_개론("20192");
        Lecture lecture2 = lectureFixture.영어청해("20192");
        TimetableFrame frame = timetableV2Fixture.시간표4(user1, semester, lecture1, lecture2);

        Integer lectureId = lecture1.getId();

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .delete("/v2/timetables/lecture/{id}", lectureId)
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value());
    }
}
