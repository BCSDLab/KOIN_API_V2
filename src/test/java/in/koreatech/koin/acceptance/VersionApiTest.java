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
        VersionType versionType = VersionType.android;
        String versionDetail = "1.0.0";

        Version version = Version.builder()
            .version(versionDetail)
            .type(versionType)
            .build();

        versionRepository.save(version);

        // when then
        ExtractableResponse<Response> response = RestAssured
            .given()
            .log()
            .all()
            .when()
            .get("/versions/" + versionType.name())
            .then()
            .log()
            .all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        // 데이터 검증
        assertSoftly(softly -> {
            softly.assertThat(response.body().jsonPath().getString("version")).isEqualTo(versionDetail);
            softly.assertThat(response.body().jsonPath().getString("type")).isEqualTo(versionType.name());
        });

    }

}
