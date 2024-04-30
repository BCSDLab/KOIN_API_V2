package in.koreatech.koin.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetable.model.TimeTable;
import in.koreatech.koin.domain.timetable.repository.TimeTableRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.fixture.LectureFixture;
import in.koreatech.koin.fixture.SemesterFixture;
import in.koreatech.koin.fixture.TimeTableFixture;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SuppressWarnings("NonAsciiCharacters")
class TimetableApiTest extends AcceptanceTest {

    @Autowired
    private TimeTableRepository timeTableRepository;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private LectureFixture lectureFixture;

    @Autowired
    private SemesterFixture semesterFixture;

    @Autowired
    private TimeTableFixture timeTableFixture;

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
        TimeTable 이산수학 = timeTableFixture.이산수학(user, semester);
        TimeTable 알고리즘및실습 = timeTableFixture.알고리즘및실습(user, semester);
        TimeTable 컴퓨터구조 = timeTableFixture.컴퓨터구조(user, semester);

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
                            "id": %d,
                            "regular_number": "40",
                            "code": "CSE125",
                            "design_score": "0",
                            "class_time": [
                                14, 15, 16, 17, 312, 313
                            ],
                            "class_place": null,
                            "memo": null,
                            "grades": "3",
                            "class_title": "이산수학",
                            "lecture_class": "01",
                            "target": "컴부전체",
                            "professor": "서정빈",
                            "department": "컴퓨터공학부"
                        },
                        {
                            "id": %d,
                            "regular_number": "32",
                            "code": "CSE130",
                            "design_score": "0",
                            "class_time": [
                                14, 15, 16, 17, 310, 311, 312, 313
                            ],
                            "class_place": null,
                            "memo": null,
                            "grades": "3",
                            "class_title": "알고리즘및실습",
                            "lecture_class": "03",
                            "target": "컴부전체",
                            "professor": "박다희",
                            "department": "컴퓨터공학부"
                        },
                        {
                            "id": %d,
                            "regular_number": "28",
                            "code": "CS101",
                            "design_score": "0",
                            "class_time": [
                                14, 15, 16, 17, 204, 205, 206, 207
                            ],
                            "class_place": null,
                            "memo": null,
                            "grades": "3",
                            "class_title": "컴퓨터 구조",
                            "lecture_class": "02",
                            "target": "컴부전체",
                            "professor": "김성재",
                            "department": "컴퓨터공학부"
                        }
                    ],
                    "grades": 9,
                    "total_grades": 9
                }
                """, 이산수학.getId(), 알고리즘및실습.getId(), 컴퓨터구조.getId()
            ));
    }

    @Test
    @DisplayName("조회된 시간표가 없으면 404에러를 반환한다.")
    void getTimeTablesNotFound() {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");
        timeTableFixture.이산수학(user, semester);
        timeTableFixture.알고리즘및실습(user, semester);
        timeTableFixture.컴퓨터구조(user, semester);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .param("semester", "20231")
            .get("/timetables")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .extract();
    }

    @Test
    @DisplayName("시간표를 생성한다.")
    void createTimeTables() {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                  "timetable": [
                    {
                      "code": "CPC490",
                      "class_title": "운영체제",
                      "class_time": [
                        210,
                        211
                      ],
                      "class_place": null,
                      "professor": "이돈우",
                      "grades": "3",
                      "lecture_class": "01",
                      "target": "디자 1 건축",
                      "regular_number": "25",
                      "design_score": "0",
                      "department": "디자인ㆍ건축공학부",
                      "memo": null
                    },
                                        {
                      "code": "CSE201",
                      "class_title": "컴퓨터구조",
                      "class_time": [
                      ],
                      "class_place": null,
                      "professor": "이강환",
                      "grades": "1",
                      "lecture_class": "02",
                      "target": "컴퓨 3",
                      "regular_number": "38",
                      "design_score": "0",
                      "department": "컴퓨터공학부",
                      "memo": null
                    }
                  ],
                  "semester": "%s"
                }
                """, semester.getSemester()
            ))
            .when()
            .post("/timetables")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "semester": "20192",
                    "timetable": [
                        {
                            "id": 1,
                            "regular_number": "25",
                            "code": "CPC490",
                            "design_score": "0",
                            "class_time": [
                                210, 211
                            ],
                            "class_place": null,
                            "memo": null,
                            "grades": "3",
                            "class_title": "운영체제",
                            "lecture_class": "01",
                            "target": "디자 1 건축",
                            "professor": "이돈우",
                            "department": "디자인ㆍ건축공학부"
                        },
                        {
                            "id": 2,
                            "regular_number": "38",
                            "code": "CSE201",
                            "design_score": "0",
                            "class_time": [

                            ],
                            "class_place": null,
                            "memo": null,
                            "grades": "1",
                            "class_title": "컴퓨터구조",
                            "lecture_class": "02",
                            "target": "컴퓨 3",
                            "professor": "이강환",
                            "department": "컴퓨터공학부"
                        }
                    ],
                    "grades": 4,
                    "total_grades": 4
                }
                """);
    }

    @Test
    @DisplayName("시간표 생성시 필수 필드를 안넣을때 에러코드 400을 반환한다.")
    void createTimeTablesBadRequest() {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                  "timetable": [
                    {
                      "code": "CPC490",
                      "class_title": null,
                      "class_time": [
                        210,
                        211
                      ],
                      "class_place": null,
                      "professor": null,
                      "grades": null,
                      "lecture_class": "01",
                      "target": "디자 1 건축",
                      "regular_number": "25",
                      "design_score": "0",
                      "department": "디자인ㆍ건축공학부",
                      "memo": null
                    }
                  ],
                  "semester": "%s"
                }
                """, semester.getSemester()
            ))
            .when()
            .post("/timetables")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract();
    }

    @Test
    @DisplayName("시간표 수정한다.")
    void updateTimeTables() {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");
        TimeTable timeTable1 = timeTableFixture.이산수학(user, semester);
        TimeTable timeTable2 = timeTableFixture.알고리즘및실습(user, semester);

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                  "timetable": [
                    {
                      "id": %d,
                      "code": "CPC999",
                      "class_title": "안녕체제",
                      "class_time": [
                        210, 211
                      ],
                      "class_place": null,
                      "professor": "차은우",
                      "grades": "1",
                      "lecture_class": "01",
                      "target": "전체",
                      "regular_number": "25",
                      "design_score": "0",
                      "department": "교양학부",
                      "memo": null
                    },
                                        {
                      "id": %d,
                      "code": "CSE777",
                      "class_title": "구조화된컴퓨터",
                      "class_time": [
                      ],
                      "class_place": null,
                      "professor": "장원영",
                      "grades": "1",
                      "lecture_class": "02",
                      "target": "컴퓨 3",
                      "regular_number": "38",
                      "design_score": "0",
                      "department": "컴퓨터공학부",
                      "memo": null
                    }
                  ],
                  "semester": "%s"
                }
                """, timeTable1.getId(), timeTable2.getId(), semester.getSemester()
            ))
            .when()
            .put("/timetables")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "semester": "20192",
                    "timetable": [
                        {
                            "id": 1,
                            "regular_number": "25",
                            "code": "CPC999",
                            "design_score": "0",
                            "class_time": [
                                210, 211
                            ],
                            "class_place": null,
                            "memo": null,
                            "grades": "1",
                            "class_title": "안녕체제",
                            "lecture_class": "01",
                            "target": "전체",
                            "professor": "차은우",
                            "department": "교양학부"
                        },
                        {
                            "id": 2,
                            "regular_number": "38",
                            "code": "CSE777",
                            "design_score": "0",
                            "class_time": [
                               
                            ],
                            "class_place": null,
                            "memo": null,
                            "grades": "1",
                            "class_title": "구조화된컴퓨터",
                            "lecture_class": "02",
                            "target": "컴퓨 3",
                            "professor": "장원영",
                            "department": "컴퓨터공학부"
                        }
                    ],
                    "grades": 2,
                    "total_grades": 2
                }
                """);
    }

    @Test
    @DisplayName("시간표를 삭제한다.")
    void deleteTimeTable() {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");
        TimeTable timeTable = timeTableFixture.이산수학(user, semester);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .param("id", timeTable.getId())
            .delete("/timetable")
            .then()
            .statusCode(HttpStatus.OK.value());

        assertThat(timeTableRepository.findById(timeTable.getId())).isNotPresent();
    }

    @Test
    @DisplayName("시간표 삭제 실패시(=조회 실패시) 404 에러코드를 반환한다.")
    void deleteTimeTableNotFound() {
        User user = userFixture.준호_학생().getUser();
        String token = userFixture.getToken(user);
        Semester semester = semesterFixture.semester("20192");
        timeTableFixture.이산수학(user, semester);
        timeTableFixture.알고리즘및실습(user, semester);
        timeTableFixture.컴퓨터구조(user, semester);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .param("id", 999)
            .delete("/timetable")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
