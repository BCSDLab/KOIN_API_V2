package in.koreatech.koin.acceptance;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.TimeTable.model.Semester;
import in.koreatech.koin.domain.TimeTable.repository.SemesterRepository;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

public class TimetableApiTest extends AcceptanceTest {

    @Autowired
    private SemesterRepository semesterRepository;

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
