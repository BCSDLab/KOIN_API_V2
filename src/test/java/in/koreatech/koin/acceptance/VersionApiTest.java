package in.koreatech.koin.acceptance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionType;
import in.koreatech.koin.domain.version.repository.VersionRepository;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;

class VersionApiTest extends AcceptanceTest {

    @Autowired
    private VersionRepository versionRepository;

    @Test
    @DisplayName("버전 타입을 통해 버전 정보를 조회한다.")
    void findVersionByType() {
        Version version = versionRepository.save(
            Version.builder()
                .version("1.0.0")
                .type(VersionType.ANDROID.getValue())
                .build()
        );

        // when then
        var response = RestAssured
            .given()
            .when()
            .get("/versions/" + version.getType())
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
                {
                    "id": %d,
                    "version": "1.0.0",
                    "type": "android",
                    "created_at": "2024-01-15 12:00:00",
                    "updated_at": "2024-01-15"
                }
                """, version.getId()
            ));
    }


    @Test
    @DisplayName("버전 타입을 통해 버전 정보를 조회한다. - 저장되지 않은 버전 타입을 요청한 경우 에러가 발생한다.")
    void findVersionByTypeError() {
        VersionType failureType = VersionType.TIMETABLE;
        RestAssured
            .given()
            .when()
            .get("/versions/" + failureType.getValue())
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .extract();

        String undefinedType = "undefined";
        RestAssured
            .given()
            .when()
            .get("/versions/" + undefinedType)
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .extract();
    }
}
