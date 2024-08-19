package in.koreatech.koin.acceptance;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.community.keywords.model.ArticleKeywordUserMap;
import in.koreatech.koin.domain.community.keywords.repository.ArticleKeywordRepository;
import in.koreatech.koin.domain.community.keywords.repository.ArticleKeywordUserMapRepository;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.fixture.KeywordFixture;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SuppressWarnings("NonAsciiCharacters")
public class KeywordApiTest extends AcceptanceTest {

    @Autowired
    private ArticleKeywordRepository articleKeywordRepository;

    @Autowired
    private ArticleKeywordUserMapRepository articleKeywordUserMapRepository;

    @Autowired
    private KeywordFixture keywordFixture;

    @Autowired
    private UserFixture userFixture;

    Student student;

    @BeforeEach
    void givenBeforeEach() {
        student = userFixture.준호_학생();
    }

    @Test
    @DisplayName("알림 키워드를 추가한다.")
    void 알림_키워드_추가() {
        String token = userFixture.getToken(student.getUser());

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
                {
                    "keyword": "장학금"
                }
                """)
            .when()
            .post("/articles/keyword")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                  "id": 1,
                  "keyword": "장학금"
                }
                """);
    }

    @Test
    @DisplayName("알림 키워드를 추가한다. - 10개 넘어가면 400에러 반환")
    void 알림_키워드_10개_넘게_추가시_에러() {
        String token = userFixture.getToken(student.getUser());

        for (int i = 0; i < 10; i++) {
            RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(String.format("""
                        {
                        "keyword": "keyword%d"
                        }
                    """, i))

                .when()
                .post("/articles/keyword")
                .then()
                .statusCode(HttpStatus.OK.value());
        }

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
                {
                    "keyword": "장학금"
                }
                """)
            .when()
            .post("/articles/keyword")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("알림 키워드를 삭제한다.")
    void 알림_키워드_삭제() {
        String token = userFixture.getToken(student.getUser());
        ArticleKeywordUserMap articleKeywordUserMap = keywordFixture.키워드1("수강 신청", student.getUser());

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .pathParam("id", articleKeywordUserMap.getId())
            .contentType(ContentType.JSON)
            .when()
            .delete("/articles/keyword/{id}")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract()
            .asString();

        assertThat(articleKeywordUserMapRepository.findById(articleKeywordUserMap.getId()).isEmpty());
        assertThat(articleKeywordRepository.findById(articleKeywordUserMap.getArticleKeyword().getId()).isEmpty());
    }

    @Test
    @DisplayName("자신의 알림 키워드를 조회한다.")
    void 자신의_알림_키워드_조회() {
        String token = userFixture.getToken(student.getUser());
        ArticleKeywordUserMap articleKeywordUserMap1 = keywordFixture.키워드1("수강신청", student.getUser());
        ArticleKeywordUserMap articleKeywordUserMap2 = keywordFixture.키워드1("장학금", student.getUser());
        ArticleKeywordUserMap articleKeywordUserMap3 = keywordFixture.키워드1("생활관", student.getUser());

        var response = RestAssured
            .given()
            .when()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .get("/articles/keyword/me")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "count": 3,
                    "keywords": [
                        {
                        "id": 1,
                        "keyword": "수강신청"
                        },
                        {
                        "id": 2,
                        "keyword": "장학금"
                        },
                        {
                        "id": 3,
                        "keyword": "생활관"
                        }
                    ]
                }""");
    }

    @Test
    @DisplayName("자신의 알림 키워드를 조회한다. - 사용자가 아무것도 추가하지 않았으면 빈 리스트 반환")
    void 자신의_알림_키워드_조회_빈리스트_반환() {
        String token = userFixture.getToken(student.getUser());

        var response = RestAssured
            .given()
            .when()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .get("/articles/keyword/me")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "count": 0,
                    "keywords": []
                }""");
    }
}
