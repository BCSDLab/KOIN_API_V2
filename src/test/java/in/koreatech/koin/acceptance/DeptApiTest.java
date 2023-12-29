package in.koreatech.koin.acceptance;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.dept.domain.Dept;
import in.koreatech.koin.domain.dept.domain.DeptNum;
import in.koreatech.koin.domain.dept.repository.DeptNumRepository;
import in.koreatech.koin.domain.dept.repository.DeptRepository;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class DeptApiTest extends AcceptanceTest {

    @Autowired
    private DeptRepository deptRepository;

    @Autowired
    private DeptNumRepository deptNumRepository;

    @Test
    @DisplayName("학과 번호를 통해 학과 이름을 조회한다.")
    void findDeptNameByDeptNumber() {
        // given
        Dept dept = Dept.builder()
            .name("컴퓨터공학부")
            .curriculumLink("https://cse.koreatech.ac.kr/page_izgw21")
            .build();

        deptRepository.save(dept);

        DeptNum deptNum1 = DeptNum.builder()
            .dept(dept)
            .number(35L)
            .build();
        DeptNum deptNum2 = DeptNum.builder()
            .dept(dept)
            .number(36L)
            .build();

        deptNumRepository.save(deptNum1);
        deptNumRepository.save(deptNum2);

        // when then
        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .when()
            .log().all()
            .param("dept_num", deptNum1.getNumber())
            .get("/dept")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getLong("dept_num")).isEqualTo(deptNum1.getNumber());
                softly.assertThat(response.body().jsonPath().getString("name")).isEqualTo(dept.getName());
            }
        );
    }
}
