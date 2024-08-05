package in.koreatech.koin.acceptance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.fixture.AbtestFixture;
import in.koreatech.koin.fixture.UserFixture;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SuppressWarnings("NonAsciiCharacters")
public class AbtestApiTest extends AcceptanceTest {

    @Autowired
    private AbtestFixture abtestFixture;

    @Autowired
    private UserFixture userFixture;

    @Test
    @DisplayName("실험을 생성한다.")
    void createAbtest() {
        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        abtestFixture.식단_UI_실험();

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer" + token)
            .contentType(ContentType.JSON)
            .body(String.format("""
                
                """))
    }

}
