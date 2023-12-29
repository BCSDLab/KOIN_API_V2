package in.koreatech.koin.acceptance;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Test
    @DisplayName("모든 학과 정보를 조회한다.")
    void findAllDepts() {
        //given
        final int DEPT_SIZE = 2;

        Dept dept1 = Dept.builder()
            .name("컴퓨터공학부")
            .curriculumLink("https://cse.koreatech.ac.kr/page_izgw21")
            .build();
        Dept dept2 = Dept.builder()
            .name("기계공학부")
            .curriculumLink("https://cms3.koreatech.ac.kr/me/795/subview.do")
            .build();

        deptRepository.save(dept1);
        deptRepository.save(dept2);

        DeptNum deptNum1_1 = DeptNum.builder()
            .dept(dept1)
            .number(35L)
            .build();
        DeptNum deptNum1_2 = DeptNum.builder()
            .dept(dept1)
            .number(36L)
            .build();
        DeptNum deptNum2 = DeptNum.builder()
            .dept(dept2)
            .number(20L)
            .build();

        deptNumRepository.save(deptNum1_1);
        deptNumRepository.save(deptNum1_2);
        deptNumRepository.save(deptNum2);

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
                softly.assertThat(response.body().jsonPath().getList(".").size()).isEqualTo(2);

                List<Boolean> deptNamesExists = Stream.generate(() -> false).limit(DEPT_SIZE).collect(Collectors.toList());
                for (int i = 0; i < DEPT_SIZE; i++) {
                    if (response.body().jsonPath().getString("[" + i + "].name").equals(dept1.getName())) {
                        deptNamesExists.set(i, true);
                        softly.assertThat(response.body().jsonPath().getString("["+ i + "].curriculum_link")).isEqualTo(dept1.getCurriculumLink());
                        softly.assertThat(parseLong(response.body().jsonPath().getString("["+ i + "].dept_nums[0]"))).isEqualTo(deptNum1_1.getNumber());
                        softly.assertThat(parseLong(response.body().jsonPath().getString("["+ i + "].dept_nums[1]"))).isEqualTo(deptNum1_2.getNumber());
                    }
                    if (response.body().jsonPath().getString("[" + i + "].name").equals(dept2.getName())) {
                        deptNamesExists.set(i, true);
                        softly.assertThat(response.body().jsonPath().getString("[" + i + "].curriculum_link")).isEqualTo(dept2.getCurriculumLink());
                        softly.assertThat(parseLong(response.body().jsonPath().getString("[" + i + "].dept_nums"))).isEqualTo(deptNum2.getNumber());
                    }
                }
                softly.assertThat(deptNamesExists.contains(false)).isFalse();
            }
        );
    }

    private Long parseLong(String jsonString) {
        return Long.valueOf(jsonString.replaceAll("[\\[\\]]", ""));
    }
}
