package in.koreatech.koin.acceptance;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.dept.model.Dept;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class DeptApiTest extends AcceptanceTest {

    @Test
    @DisplayName("학과 번호를 통해 학과 이름을 조회한다.")
    void findDeptNameByDeptNumber() {
        // given
        Dept dept = Dept.COMPUTER_SCIENCE;

        // when then
        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .when()
            .log().all()
            .param("dept_num", dept.getNumbers().get(0))
            .get("/dept")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getLong("dept_num")).isEqualTo(dept.getNumbers().get(0));
                softly.assertThat(response.body().jsonPath().getString("name")).isEqualTo(dept.getName());
            }
        );
    }

    @Test
    @DisplayName("모든 학과 정보를 조회한다.")
    void findAllDepts() {
        //given
        final int DEPT_SIZE = Dept.values().length;

        //when then
        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .when()
            .log().all()
            .get("/depts")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getList(".").size())
                    .isEqualTo(DEPT_SIZE);
                for (int i = 0; i < DEPT_SIZE; i++) {
                    softly.assertThat(response.body().jsonPath().getString(String.format("[%d].name", i))).isNotEmpty();
                    softly.assertThat(response.body().jsonPath().getString(String.format("[%d].curriculum_link", i))).isNotEmpty();
                    softly.assertThat(response.body().jsonPath().getString(String.format("[%d].dept_nums[0]", i))).isNotEmpty();
                }
            }
        );
    }
}
