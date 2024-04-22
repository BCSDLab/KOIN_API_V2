package in.koreatech.koin.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.dept.model.Dept;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;

class DeptApiTest extends AcceptanceTest {

    @Test
    @DisplayName("학과 번호를 통해 학과 이름을 조회한다.")
    void findDeptNameByDeptNumber() {
        // given
        Dept dept = Dept.COMPUTER_SCIENCE;

        // when then
        var response = RestAssured
            .given()
            .when()
            .param("dept_num", dept.getNumbers().get(0))
            .get("/dept")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "dept_num": "35",
                    "name": "컴퓨터공학부"
                }
                """
            );
    }

    @Test
    @DisplayName("모든 학과 정보를 조회한다.")
    void findAllDepts() {
        //given
        final int DEPT_SIZE = Dept.values().length - 1;

        //when then
        var response = RestAssured
            .given()
            .when()
            .get("/depts")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertThat(response.body().jsonPath().getList(".")).hasSize(DEPT_SIZE);
    }
}
