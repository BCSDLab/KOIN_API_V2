package in.koreatech.koin.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordUserMap;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordRepository;
import in.koreatech.koin.domain.community.keyword.repository.ArticleKeywordUserMapRepository;
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
    private UserFixture userFixture;

    @Autowired
    private KeywordFixture keywordFixture;

    Student student;

    @BeforeEach
    void givenBeforeEach() {
        student = userFixture.준호_학생();
    }

    @Test
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
    void 알림_키워드_10개_넘게_추가시_400에러_반환() {
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
    void 자신의_알림_키워드_조회() {
        String token = userFixture.getToken(student.getUser());
        keywordFixture.키워드1("수강신청", student.getUser());
        keywordFixture.키워드1("장학금", student.getUser());
        keywordFixture.키워드1("생활관", student.getUser());

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
    void 자신의_알림_키워드_조회_사용자가_아무것도_추가_안했으면_빈리스트_반환() {
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
