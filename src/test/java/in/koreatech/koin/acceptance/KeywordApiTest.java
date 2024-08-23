package in.koreatech.koin.acceptance;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.community.keywords.model.ArticleKeywordSuggestCache;
import in.koreatech.koin.domain.community.keywords.model.ArticleKeywordUserMap;
import in.koreatech.koin.domain.community.keywords.repository.ArticleKeywordRepository;
import in.koreatech.koin.domain.community.keywords.repository.ArticleKeywordSuggestRepository;
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
    private ArticleKeywordSuggestRepository articleKeywordSuggestRepository;

    @Autowired
    private KeywordFixture keywordFixture;

    @Autowired
    private UserFixture userFixture;

    @Test
    void 알림_키워드_추가() {
        Student student = userFixture.준호_학생();
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
    void 알림_키워드_10개_넘게_추가시_400에러() {
        Student student = userFixture.준호_학생();
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
        Student student = userFixture.준호_학생();
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
        Student student = userFixture.준호_학생();
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
    void 사용자_아무_것도_추가_안_했을_때_자신의_알림_키워드_조회_빈_리스트_반환() {
        Student student = userFixture.준호_학생();
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

    @Test
    void 사용자가_추가_한_키워드는_제외_하고_가장_인기_있는_키워드_추천() {
        Student student = userFixture.준호_학생();
        String token1 = userFixture.getToken(student.getUser());

        for (int i = 1; i <= 10; i++) {
            RestAssured
                .given()
                .header("Authorization", "Bearer " + token1)
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "keyword": "수강신청%s"
                    }
                    """.formatted(i))
                .when()
                .post("/articles/keyword")
                .then()
                .statusCode(HttpStatus.OK.value());
        }

        // Redis에 인기 키워드 15개 저장
        List<ArticleKeywordSuggestCache> hotKeywords = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            hotKeywords.add(ArticleKeywordSuggestCache.builder()
                .hotKeywordId(i)
                .keyword("수강신청" + i)
                .build());
        }

        hotKeywords.forEach(keyword -> articleKeywordSuggestRepository.save(keyword));

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token1)
            .contentType(ContentType.JSON)
            .when()
            .get("/articles/keyword/suggestions")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .asPrettyString();

        JsonAssertions.assertThat(response).isEqualTo("""
                {
                  "keywords": [
                    "수강신청11", "수강신청12", "수강신청13", "수강신청14", "수강신청15"
                  ]
                }
            """);
    }
}
