package in.koreatech.koin.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetable.repository.TimetableRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.fixture.LectureFixture;
import in.koreatech.koin.fixture.SemesterFixture;
import in.koreatech.koin.fixture.TimeTableV2Fixture;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SuppressWarnings("NonAsciiCharacters")
class TimetableApiTest extends AcceptanceTest {

    @Autowired
    private TimeTableV2Fixture timetableV2Fixture;

    @Autowired
    private TimetableRepository timetableRepository;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private LectureFixture lectureFixture;

    @Autowired
    private SemesterFixture semesterFixture;

    @Test
    @DisplayName("특정 학기 강의를 조회한다")
    void getSemesterLecture() {
        String semester = "20201";
        lectureFixture.HRD_개론(semester);
        lectureFixture.건축구조의_이해_및_실습("20192");

        var response = RestAssured
            .given()
            .when()
            .param("semester_date", semester)
            .get("/lectures")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                [
                    {
                        "id": 1,
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
                """);
    }

    @Test
    @DisplayName("특정 학기 강의들을 조회한다")
    void getSemesterLectures() {
        String semester = "20201";
        lectureFixture.HRD_개론(semester);
        lectureFixture.건축구조의_이해_및_실습(semester);
        lectureFixture.재료역학(semester);

        var response = RestAssured
            .given()
            .when()
            .param("semester_date", semester)
            .get("/lectures")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                [
                    {
                        "id": 1,
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
                        "id": 2,
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
                        "id": 3,
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
                """);
    }

    @Test
    @DisplayName("존재하지 않는 학기를 조회하면 404")
    void isNotSemester() {
        String semester = "20201";
        lectureFixture.HRD_개론(semester);
        lectureFixture.건축구조의_이해_및_실습(semester);

        RestAssured
            .given()
            .when()
            .param("semester_date", "20193")
            .get("/lectures")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .extract();
    }

    @Test
    @DisplayName("모든 학기를 조회한다.")
    void findAllSemesters() {
        semesterFixture.semester("20221");
        semesterFixture.semester("20222");
        semesterFixture.semester("20231");
        semesterFixture.semester("20232");

        var response = RestAssured
            .given()
            .when()
            .get("/semesters")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                [
                    {
                        "id": 4,
                        "semester": "20232"
                    },
                    {
                        "id": 3,
                        "semester": "20231"
                    },
                    {
                        "id": 2,
                        "semester": "20222"
                    },
                    {
                        "id": 1,
                        "semester": "20221"
                    }
                ]
                """);
    }

    @Test
    @DisplayName("시간표를 조회한다.")
    void getTimeTables() {
        // given
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");

        Lecture 건축구조의_이해_및_실습 = lectureFixture.건축구조의_이해_및_실습(semester.getSemester());
        Lecture HRD_개론 = lectureFixture.HRD_개론(semester.getSemester());

        timetableV2Fixture.시간표6(user, semester, 건축구조의_이해_및_실습, HRD_개론);

        // when & then
        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .param("semester", semester.getSemester())
            .get("/timetables")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
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
                """
            ));
    }

    @Test
    @DisplayName("학생이 가진 시간표의 학기를 조회한다.")
    void getStudentCheckSemester() {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester1 = semesterFixture.semester("20192");
        Semester semester2 = semesterFixture.semester("20201");
        Lecture HRD_개론 = lectureFixture.HRD_개론(semester1.getSemester());
        Lecture 건축구조의_이해_및_실습 = lectureFixture.건축구조의_이해_및_실습(semester2.getSemester());
        timetableV2Fixture.시간표6(user, semester1, HRD_개론, null);
        timetableV2Fixture.시간표6(user, semester2, 건축구조의_이해_및_실습, null);


        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/semesters/check")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "user_id": 1,
                    "semesters": [
                      "20192",
                      "20201"
                    ]
                }
                """
            );
    }

    @Test
    @DisplayName("시간표를 생성한다.")
    void createTimeTables() {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");

       lectureFixture.건축구조의_이해_및_실습(semester.getSemester());
       lectureFixture.HRD_개론(semester.getSemester());

        timetableV2Fixture.시간표1(user, semester);

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
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
            .when()
            .post("/timetables")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .response();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
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
        """);
    }

    @Test
    @DisplayName("시간표를 삭제한다.")
    void deleteTimetable() {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");

        Lecture 건축구조의_이해_및_실습 = lectureFixture.건축구조의_이해_및_실습(semester.getSemester());
        Lecture HRD_개론 = lectureFixture.HRD_개론(semester.getSemester());

        timetableV2Fixture.시간표6(user, semester, 건축구조의_이해_및_실습, HRD_개론);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .param("id", 2)
            .delete("/timetable")
            .then()
            .statusCode(HttpStatus.OK.value());

        assertThat(timetableRepository.findById(2)).isNotPresent();
    }
}
