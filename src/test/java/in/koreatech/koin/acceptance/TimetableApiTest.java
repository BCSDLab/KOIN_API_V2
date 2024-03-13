package in.koreatech.koin.acceptance;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.TimeTable.model.Semester;
import in.koreatech.koin.domain.TimeTable.repository.SemesterRepository;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.repository.LectureRepository;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class TimetableApiTest extends AcceptanceTest {

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private SemesterRepository semesterRepository;

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
        Semester semester1 = semesterRepository.save(request1);
        Semester request2 = Semester.builder().semester("20222").build();
        Semester semester2 = semesterRepository.save(request2);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .get("/semesters")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getLong("[0].id")).isEqualTo(semester1.getId());
                softly.assertThat(response.body().jsonPath().getString("[0].semester"))
                    .isEqualTo(semester1.getSemester());

                softly.assertThat(response.body().jsonPath().getLong("[1].id")).isEqualTo(semester2.getId());
                softly.assertThat(response.body().jsonPath().getString("[1].semester"))
                    .isEqualTo(semester2.getSemester());
            }
        );
    }
}
