package in.koreatech.koin.acceptance;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.repository.VersionRepository;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class VersionApiTest extends AcceptanceTest {

    @Autowired
    private VersionRepository versionRepository;

    @Test
    @DisplayName("버전 타입을 통해 버전 정보를 조회한다.")
    void findVersionByType() {
        VersionType versionType = VersionType.ANDROID;
        String versionDetail = "1.0.0";

        Version version = Version.builder()
            .version(versionDetail)
            .type(versionType)
            .build();

        versionRepository.save(version);

        // when then
        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .when()
            .get("/versions/" + versionType.getValue())
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        // 데이터 검증
        assertSoftly(softly -> {
            softly.assertThat(response.body().jsonPath().getString("version")).isEqualTo(versionDetail);
            softly.assertThat(response.body().jsonPath().getString("type")).isEqualTo(versionType.getValue());
        });

        // 실패 케이스 설정
        VersionType failureType = VersionType.TIMETABLE;

        // 실패 케이스 테스트
        ExtractableResponse<Response> notFoundFailureResponse = RestAssured
            .given()
            .log().all()
            .when()
            .get("/versions/" + failureType.getValue())
            .then()
            .log().all()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .extract();

        // 존재하지 않는 Enum 케이스
        String undefinedType = "undefined";

        // 정의되지 않은 버전 타입으로 API 호출
        ExtractableResponse<Response> enumTypeFailureResponse = RestAssured
            .given()
            .log().all()
            .when()
            .get("/versions/" + undefinedType)
            .then()
            .log().all()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract();

    }

}
