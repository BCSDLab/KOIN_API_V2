package in.koreatech.koin.acceptance;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetable.model.TimeTable;
import in.koreatech.koin.domain.timetable.repository.LectureRepository;
import in.koreatech.koin.domain.timetable.repository.SemesterRepository;
import in.koreatech.koin.domain.timetable.repository.TimeTableRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class TimetableApiTest extends AcceptanceTest {

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private TimeTableRepository timeTableRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("특정 학기 강의를 조회한다")
    void getSemesterLecture() {
        Lecture lecture1 = Lecture.builder()
            .code("ARB244")
            .semester("20192")
            .name("건축구조의 이해 및 실습")
            .grades("3")
            .lectureClass("01")
            .regularNumber("25")
            .department("디자인ㆍ건축공학부")
            .target("디자 1 건축")
            .professor("이돈우")
            .isEnglish("N")
            .designScore("0")
            .isElearning("N")
            .classTime("[200,201,202,203,204,205,206,207]")
            .build();

        Lecture lecture2 = Lecture.builder()
            .code("ARB244")
            .semester("20201")
            .name("건축구조의 이해 및 실습")
            .grades("3")
            .lectureClass("02")
            .regularNumber("30")
            .department("컴퓨터공학부")
            .target("컴퓨터")
            .professor("허준기")
            .isEnglish("N")
            .designScore("0")
            .isElearning("N")
            .classTime("[200,201,202,203,204,205,206,207]")
            .build();

        lectureRepository.save(lecture1);
        lectureRepository.save(lecture2);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .param("semester_date", lecture1.getSemester())
            .get("/lectures")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        List<Long> classTime = List.of(200L, 201L, 202L, 203L, 204L, 205L, 206L, 207L);

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getString("[0].code")).isEqualTo(lecture1.getCode());
                softly.assertThat(response.body().jsonPath().getString("[0].name")).isEqualTo(lecture1.getName());
                softly.assertThat(response.body().jsonPath().getString("[0].grades")).isEqualTo(lecture1.getGrades());
                softly.assertThat(response.body().jsonPath().getString("[0].lecture_class"))
                    .isEqualTo(lecture1.getLectureClass());
                softly.assertThat(response.body().jsonPath().getString("[0].regular_number"))
                    .isEqualTo(lecture1.getRegularNumber());
                softly.assertThat(response.body().jsonPath().getString("[0].department"))
                    .isEqualTo(lecture1.getDepartment());
                softly.assertThat(response.body().jsonPath().getString("[0].target")).isEqualTo(lecture1.getTarget());
                softly.assertThat(response.body().jsonPath().getString("[0].professor"))
                    .isEqualTo(lecture1.getProfessor());
                softly.assertThat(response.body().jsonPath().getString("[0].is_english"))
                    .isEqualTo(lecture1.getIsEnglish());
                softly.assertThat(response.body().jsonPath().getString("[0].design_score"))
                    .isEqualTo(lecture1.getDesignScore());
                softly.assertThat(response.body().jsonPath().getString("[0].is_elearning"))
                    .isEqualTo(lecture1.getIsElearning());
                softly.assertThat(response.body().jsonPath().getList("[0].class_time", Long.class))
                    .containsExactlyInAnyOrderElementsOf(classTime);
            }
        );
    }

    @Test
    @DisplayName("특정 학기 강의들을 조회한다")
    void getSemesterLectures() {
        Lecture lecture1 = Lecture.builder()
            .code("ARB244")
            .semester("20192")
            .name("건축구조의 이해 및 실습")
            .grades("3")
            .lectureClass("01")
            .regularNumber("25")
            .department("디자인ㆍ건축공학부")
            .target("디자 1 건축")
            .professor("이돈우")
            .isEnglish("N")
            .designScore("0")
            .isElearning("N")
            .classTime("[200,201,202,203,204,205,206,207]")
            .build();

        Lecture lecture2 = Lecture.builder()
            .code("ARB244")
            .semester("20192")
            .name("건축구조의 이해 및 실습")
            .grades("3")
            .lectureClass("02")
            .regularNumber("30")
            .department("컴퓨터공학부")
            .target("컴퓨터")
            .professor("허준기")
            .isEnglish("N")
            .designScore("0")
            .isElearning("N")
            .classTime("[200,201,202,203,204,205,206,207]")
            .build();

        Lecture lecture3 = Lecture.builder()
            .code("ARB244")
            .semester("20201")
            .name("건축구조의 이해 및 실습")
            .grades("3")
            .lectureClass("02")
            .regularNumber("30")
            .department("컴퓨터공학부")
            .target("컴퓨터")
            .professor("허준기")
            .isEnglish("N")
            .designScore("0")
            .isElearning("N")
            .classTime("[200,201,202,203,204,205,206,207]")
            .build();

        lectureRepository.save(lecture1);
        lectureRepository.save(lecture2);
        lectureRepository.save(lecture3);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .param("semester_date", lecture1.getSemester())
            .get("/lectures")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> softly.assertThat(response.body().jsonPath().getList(".").size()).isEqualTo(2)
        );
    }

    @Test
    @DisplayName("존재하지 않는 학기를 조회하면 404")
    void isNotSemester() {
        Lecture lecture1 = Lecture.builder()
            .code("ARB244")
            .semester("20192")
            .name("건축구조의 이해 및 실습")
            .grades("3")
            .lectureClass("01")
            .regularNumber("25")
            .department("디자인ㆍ건축공학부")
            .target("디자 1 건축")
            .professor("이돈우")
            .isEnglish("N")
            .designScore("0")
            .isElearning("N")
            .classTime("[200,201,202,203,204,205,206,207]")
            .build();

        Lecture lecture2 = Lecture.builder()
            .code("ARB244")
            .semester("20201")
            .name("건축구조의 이해 및 실습")
            .grades("3")
            .lectureClass("02")
            .regularNumber("30")
            .department("컴퓨터공학부")
            .target("컴퓨터")
            .professor("허준기")
            .isEnglish("N")
            .designScore("0")
            .isElearning("N")
            .classTime("[200,201,202,203,204,205,206,207]")
            .build();

        lectureRepository.save(lecture1);
        lectureRepository.save(lecture2);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .param("semester_date", 20193)
            .get("/lectures")
            .then()
            .log().all()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .extract();
    }

    @Test
    @DisplayName("모든 학기를 조회한다.")
    void findAllSemesters() {
        Semester request1 = Semester.builder().semester("20221").build();
        Semester request2 = Semester.builder().semester("20222").build();
        Semester request3 = Semester.builder().semester("20231").build();
        Semester request4 = Semester.builder().semester("20232").build();
        semesterRepository.save(request1);
        semesterRepository.save(request2);
        semesterRepository.save(request3);
        semesterRepository.save(request4);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .get("/semesters")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertThat(response.body().jsonPath().getList(".")).hasSize(4);
    }

    @Test
    @DisplayName("시간표를 조회한다.")
    void getTimeTables() {
        User userT = User.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .gender(UserGender.MAN)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        User user = userRepository.save(userT);
        String token = jwtProvider.createToken(user);

        Semester semesterT = Semester.builder().
            semester("20192")
            .build();

        Semester semester = semesterRepository.save(semesterT);

        TimeTable timeTable1 = TimeTable.builder()
            .user(user)
            .semester(semester)
            .code("CS101")
            .classTitle("컴퓨터 구조")
            .classTime("[14, 15, 16, 17, 204, 205, 206, 207]")
            .classPlace(null)
            .professor("김철수")
            .grades("3")
            .lectureClass("02")
            .target("컴부전체")
            .regularNumber("28")
            .designScore("0")
            .department("컴퓨터공학부")
            .memo(null)
            .isDeleted(false)
            .build();

        TimeTable timeTable2 = TimeTable.builder()
            .user(user)
            .semester(semester)
            .code("CS102")
            .classTitle("운영체제")
            .classTime("[932]")
            .classPlace(null)
            .professor("홍길동")
            .grades("3")
            .lectureClass("01")
            .target("컴부전체")
            .regularNumber("40")
            .designScore("0")
            .department("컴퓨터공학부")
            .memo(null)
            .isDeleted(false)
            .build();

        TimeTable timeTable3 = TimeTable.builder()
            .user(user)
            .semester(semester)
            .code("CS102")
            .classTitle("운영체제")
            .classTime("[]")
            .classPlace(null)
            .professor("홍길동")
            .grades("3")
            .lectureClass("01")
            .target("컴부전체")
            .regularNumber("40")
            .designScore("0")
            .department("컴퓨터공학부")
            .memo(null)
            .isDeleted(false)
            .build();

        timeTableRepository.save(timeTable1);
        timeTableRepository.save(timeTable2);
        timeTableRepository.save(timeTable3);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .param("semester", "20192")
            .get("/timetables")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getLong("[0].id")).
                    isEqualTo(1L);
                softly.assertThat(response.body().jsonPath().getLong("[1].id")).
                    isEqualTo(2L);
                softly.assertThat(response.body().jsonPath().getLong("[2].id")).
                    isEqualTo(3L);

                softly.assertThat(response.body().jsonPath().getString("[0].code")).
                    isEqualTo(timeTable1.getCode());
                softly.assertThat(response.body().jsonPath().getString("[0].class_title")).
                    isEqualTo(timeTable1.getClassTitle());
                softly.assertThat(response.body().jsonPath().getList("[0].class_time", Integer.class))
                    .containsExactlyInAnyOrderElementsOf(List.of(14, 15, 16, 17, 204, 205, 206, 207));
                softly.assertThat(response.body().jsonPath().getString("[0].class_place"))
                    .isEqualTo(timeTable1.getClassPlace());
                softly.assertThat(response.body().jsonPath().getString("[0].professor"))
                    .isEqualTo(timeTable1.getProfessor());
                softly.assertThat(response.body().jsonPath().getString("[0].grades"))
                    .isEqualTo(timeTable1.getGrades());
                softly.assertThat(response.body().jsonPath().getString("[0].lecture_class")).
                    isEqualTo(timeTable1.getLectureClass());
                softly.assertThat(response.body().jsonPath().getString("[0].target"))
                    .isEqualTo(timeTable1.getTarget());
                softly.assertThat(response.body().jsonPath().getString("[0].regular_number"))
                    .isEqualTo(timeTable1.getRegularNumber());
                softly.assertThat(response.body().jsonPath().getString("[0].design_score"))
                    .isEqualTo(timeTable1.getDesignScore());
                softly.assertThat(response.body().jsonPath().getString("[0].department"))
                    .isEqualTo(timeTable1.getDepartment());
                softly.assertThat(response.body().jsonPath().getList("[0].memo"))
                    .isEqualTo(timeTable1.getMemo());
            }
        );
    }

    @Test
    @DisplayName("조회된 시간표가 없으면 404에러를 반환한다.")
    void getTimeTablesNotFound() {
        User userT = User.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .gender(UserGender.MAN)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        User user = userRepository.save(userT);
        String token = jwtProvider.createToken(user);

        Semester semester = Semester.builder().
            semester("20192")
            .build();

        semesterRepository.save(semester);

        ExtractableResponse<Response> response = RestAssured
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
        User userT = User.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .gender(UserGender.MAN)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        User user = userRepository.save(userT);
        String token = jwtProvider.createToken(user);

        Semester semester = Semester.builder().
            semester("20192")
            .build();

        semesterRepository.save(semester);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
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
                  "semester": "20192"
                }
                """)
            .when()
            .post("/timetables")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertThat(response.body().jsonPath().getList("timetable")).hasSize(2);

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getLong("[0].id")).
                    isEqualTo(1L);
                softly.assertThat(response.body().jsonPath().getLong("[1].id")).
                    isEqualTo(2L);

                softly.assertThat(response.body().jsonPath().getString("[0].code")).
                    isEqualTo("CPC490");
                softly.assertThat(response.body().jsonPath().getString("[0].class_title")).
                    isEqualTo("운영체제");
                softly.assertThat(response.body().jsonPath().getList("[0].class_time", Integer.class))
                    .containsExactlyInAnyOrderElementsOf(List.of(210, 211));
                softly.assertThat(response.body().jsonPath().getString("[0].class_place"))
                    .isEqualTo(null);
                softly.assertThat(response.body().jsonPath().getString("[0].professor"))
                    .isEqualTo("이돈우");
                softly.assertThat(response.body().jsonPath().getString("[0].grades"))
                    .isEqualTo("3");
                softly.assertThat(response.body().jsonPath().getString("[0].lecture_class")).
                    isEqualTo("01");
                softly.assertThat(response.body().jsonPath().getString("[0].target"))
                    .isEqualTo("디자 1 건축");
                softly.assertThat(response.body().jsonPath().getString("[0].regular_number"))
                    .isEqualTo("25");
                softly.assertThat(response.body().jsonPath().getString("[0].design_score"))
                    .isEqualTo("0");
                softly.assertThat(response.body().jsonPath().getString("[0].department"))
                    .isEqualTo("디자인ㆍ건축공학부");
                softly.assertThat(response.body().jsonPath().getList("[0].memo"))
                    .isEqualTo(null);
            }
        );
    }

    @Test
    @DisplayName("시간표 생성시 필수 필드를 안넣을때 에러코드400을 반환한다.")
    void createTimeTablesBadRequest() {
        User userT = User.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .gender(UserGender.MAN)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        User user = userRepository.save(userT);
        String token = jwtProvider.createToken(user);

        Semester semester = Semester.builder().
            semester("20192")
            .build();

        semesterRepository.save(semester);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
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
                  "semester": "20192"
                }
                """)
            .when()
            .post("/timetables")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract();
    }

    @Test
    @DisplayName("시간표 수정한다.")
    void updateTimeTables() {
        User userT = User.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .gender(UserGender.MAN)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        User user = userRepository.save(userT);
        String token = jwtProvider.createToken(user);

        Semester semesterT = Semester.builder().
            semester("20192")
            .build();

        Semester semester = semesterRepository.save(semesterT);

        TimeTable timeTable1 = TimeTable.builder()
            .user(user)
            .semester(semester)
            .code("CS101")
            .classTitle("컴퓨터 구조")
            .classTime("[14, 15, 16, 17, 204, 205, 206, 207]")
            .classPlace(null)
            .professor("김철수")
            .grades("3")
            .lectureClass("02")
            .target("컴부전체")
            .regularNumber("28")
            .designScore("0")
            .department("컴퓨터공학부")
            .memo(null)
            .isDeleted(false)
            .build();

        TimeTable timeTable2 = TimeTable.builder()
            .user(user)
            .semester(semester)
            .code("CS102")
            .classTitle("운영체제")
            .classTime("[932]")
            .classPlace(null)
            .professor("홍길동")
            .grades("3")
            .lectureClass("01")
            .target("컴부전체")
            .regularNumber("40")
            .designScore("0")
            .department("컴퓨터공학부")
            .memo(null)
            .isDeleted(false)
            .build();

        timeTableRepository.save(timeTable1);
        timeTableRepository.save(timeTable2);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
                {
                  "timetable": [
                    {
                      "id": 1,
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
                      "id": 2,
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
                  "semester": "20192"
                }
                """)
            .when()
            .put("/timetables")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertThat(response.body().jsonPath().getList("timetable")).hasSize(2);

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getLong("[0].id")).
                    isEqualTo(1L);
                softly.assertThat(response.body().jsonPath().getLong("[1].id")).
                    isEqualTo(2L);

                softly.assertThat(response.body().jsonPath().getString("[0].code")).
                    isEqualTo("CPC490");
                softly.assertThat(response.body().jsonPath().getString("[0].class_title")).
                    isEqualTo("운영체제");
                softly.assertThat(response.body().jsonPath().getList("[0].class_time", Integer.class))
                    .containsExactlyInAnyOrderElementsOf(List.of(210, 211));
                softly.assertThat(response.body().jsonPath().getString("[0].class_place"))
                    .isEqualTo(null);
                softly.assertThat(response.body().jsonPath().getString("[0].professor"))
                    .isEqualTo("이돈우");
                softly.assertThat(response.body().jsonPath().getString("[0].grades"))
                    .isEqualTo("3");
                softly.assertThat(response.body().jsonPath().getString("[0].lecture_class")).
                    isEqualTo("01");
                softly.assertThat(response.body().jsonPath().getString("[0].target"))
                    .isEqualTo("디자 1 건축");
                softly.assertThat(response.body().jsonPath().getString("[0].regular_number"))
                    .isEqualTo("25");
                softly.assertThat(response.body().jsonPath().getString("[0].design_score"))
                    .isEqualTo("0");
                softly.assertThat(response.body().jsonPath().getString("[0].department"))
                    .isEqualTo("디자인ㆍ건축공학부");
                softly.assertThat(response.body().jsonPath().getList("[0].memo"))
                    .isEqualTo(null);
            }
        );
    }

    @Test
    @DisplayName("시간표를 삭제한다.")
    void deleteTimeTable() {
        User userT = User.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .gender(UserGender.MAN)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        User user = userRepository.save(userT);
        String token = jwtProvider.createToken(user);

        Semester semesterT = Semester.builder().
            semester("20192")
            .build();

        Semester semester = semesterRepository.save(semesterT);

        TimeTable timeTable = TimeTable.builder()
            .user(user)
            .semester(semester)
            .code("CS101")
            .classTitle("컴퓨터 구조")
            .classTime("[14, 15, 16, 17, 204, 205, 206, 207]")
            .classPlace(null)
            .professor("김철수")
            .grades("3")
            .lectureClass("02")
            .target("컴부전체")
            .regularNumber("28")
            .designScore("0")
            .department("컴퓨터공학부")
            .memo(null)
            .isDeleted(false)
            .build();

        timeTableRepository.save(timeTable);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .param("id", 1L)
            .delete("/timetable")
            .then()
            .statusCode(HttpStatus.OK.value());

        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .param("semester", "20192")
            .get("/timetables")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertThat(response.body().jsonPath().getList("timetable")).hasSize(0);
    }

    @Test
    @DisplayName("시간표 삭제 실패시(=조회 실패시) 404 에러코드를 반환한다.")
    void deleteTimeTableNotFound() {
        User userT = User.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .gender(UserGender.MAN)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        User user = userRepository.save(userT);
        String token = jwtProvider.createToken(user);

        Semester semesterT = Semester.builder().
            semester("20192")
            .build();

        Semester semester = semesterRepository.save(semesterT);

        TimeTable timeTable = TimeTable.builder()
            .user(user)
            .semester(semester)
            .code("CS101")
            .classTitle("컴퓨터 구조")
            .classTime("[14, 15, 16, 17, 204, 205, 206, 207]")
            .classPlace(null)
            .professor("김철수")
            .grades("3")
            .lectureClass("02")
            .target("컴부전체")
            .regularNumber("28")
            .designScore("0")
            .department("컴퓨터공학부")
            .memo(null)
            .isDeleted(false)
            .build();

        timeTableRepository.save(timeTable);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .param("id", 3)
            .delete("/timetable")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
