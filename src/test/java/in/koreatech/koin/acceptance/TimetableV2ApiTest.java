package in.koreatech.koin.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetable.model.TimeTableFrame;
import in.koreatech.koin.domain.timetable.repository.TimeTableFrameRepository;
import in.koreatech.koin.domain.timetable.repository.TimeTableRepository;
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
    private TimeTableV2Fixture timeTableV2Fixture;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private SemesterFixture semesterFixture;

    @Autowired
    private LectureFixture lectureFixture;

    @Autowired
    private TimeTableFrameRepository timeTableFrameRepository;

    @Autowired
    private TimeTableRepository timeTableRepository;

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
            .post("/timetables/frame")
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
    @DisplayName("모든 시간표 frame을 조회한다")
    void getAllTimeTablesFrame() {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");

        timeTableV2Fixture.시간표1(user, semester);
        timeTableV2Fixture.시간표2(user, semester);

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .param("semester", semester.getSemester())
            .get("/timetables/frame")
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
    @DisplayName("시간표 정보를 갖고 있는 특정 시간표 frame을 삭제한다")
    void deleteTimeTablesFrame() {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");
        Lecture lecture = lectureFixture.HRD_개론(semester.getSemester());

        TimeTableFrame frame1 = timeTableV2Fixture.시간표5(user, semester, lecture);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .param("id", frame1.getId())
            .delete("/timetables/frame")
            .then()
            .statusCode(HttpStatus.OK.value());

        assertThat(timeTableFrameRepository.findById(frame1.getId())).isNotPresent();
        assertThat(timeTableRepository.findById(frame1.getTimeTableLectures().get(1).getId())).isNotPresent();
    }

    @Test
    @DisplayName("특정 시간표 frame을 삭제한다 - 본인 삭제가 아니면 403 반환")
    void deleteTimeTablesFrameNoAuth() {
        User user1 = userFixture.준호_학생().getUser();
        User user2 = userFixture.성빈_학생().getUser();
        String token = userFixture.getToken(user2);
        Semester semester = semesterFixture.semester("20192");

        TimeTableFrame frame1 = timeTableV2Fixture.시간표1(user1, semester);
        TimeTableFrame frame2 = timeTableV2Fixture.시간표2(user1, semester);

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .param("id", frame1.getId())
            .delete("/timetables/frame")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value());
    }
}
