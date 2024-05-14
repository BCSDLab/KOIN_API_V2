package in.koreatech.koin.admin.acceptance;

import static in.koreatech.koin.domain.user.model.UserGender.MAN;
import static in.koreatech.koin.domain.user.model.UserType.OWNER;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.admin.user.repository.AdminOwnerRepository;
import in.koreatech.koin.admin.user.repository.AdminStudentRepository;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerAttachment;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.fixture.ShopFixture;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SuppressWarnings("NonAsciiCharacters")
public class AdminUserApiTest extends AcceptanceTest {

    @Autowired
    private AdminStudentRepository adminStudentRepository;

    @Autowired
    private AdminOwnerRepository adminOwnerRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private ShopFixture shopFixture;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("관리자가 특정 학생 정보를 수정한다. - 관리자가 아니면 403 반환")
    void studentUpdateAdminNoAuth() {
        Student student = userFixture.준호_학생();

        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(student.getUser());

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
                  {
                    "gender" : 1,
                    "major" : "기계공학부",
                    "name" : "서정빈",
                    "password" : "0c4be6acaba1839d3433c1ccf04e1eec4d1fa841ee37cb019addc269e8bc1b77",
                    "nickname" : "duehee",
                    "phone_number" : "010-2345-6789",
                    "student_number" : "2019136136"
                  }
                """)
            .when()
            .pathParam("id", student.getUser().getId())
            .put("/admin/users/student/{id}")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.value())
            .extract();
    }

    @Test
    @DisplayName("관리자가 특정 학생 정보를 수정한다.")
    void studentUpdateAdmin() {
        Student student = userFixture.준호_학생();

        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
                  {
                    "gender" : 1,
                    "major" : "기계공학부",
                    "name" : "서정빈",
                    "password" : "0c4be6acaba1839d3433c1ccf04e1eec4d1fa841ee37cb019addc269e8bc1b77",
                    "nickname" : "duehee",
                    "phone_number" : "010-2345-6789",
                    "student_number" : "2019136136"
                  }
                """)
            .when()
            .pathParam("id", student.getUser().getId())
            .put("/admin/users/student/{id}")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        transactionTemplate.executeWithoutResult(status -> {
            Student result = adminStudentRepository.getById(student.getId());
            SoftAssertions.assertSoftly(
                softly -> {
                    softly.assertThat(result.getUser().getName()).isEqualTo("서정빈");
                    softly.assertThat(result.getUser().getNickname()).isEqualTo("duehee");
                    softly.assertThat(result.getUser().getGender()).isEqualTo(UserGender.from(1));
                    softly.assertThat(result.getStudentNumber()).isEqualTo("2019136136");
                }
            );
        });

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "anonymous_nickname": "익명",
                    "email": "juno@koreatech.ac.kr",
                    "gender": 1,
                    "major": "기계공학부",
                    "name": "서정빈",
                    "nickname": "duehee",
                    "phone_number": "010-2345-6789",
                    "student_number": "2019136136"
                }
                """);
    }

    @Test
    @DisplayName("관리자가 가입 신청한 사장님 리스트 조회한다.")
    void getNewOwnersAdmin() {
        Owner unauthenticatedOwner = userFixture.철수_사장님();
        Owner authenticatedOwner = userFixture.준영_사장님();

        Shop shopA = shopFixture.마슬랜(unauthenticatedOwner);
        Shop shopB = shopFixture.신전_떡볶이(unauthenticatedOwner);

        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .param("searchType", "NAME")
            .param("query", "철수")
            .param("sort", "CREATED_AT_DESC")
            .get("/admin/users/new-owners")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
                {
                    "total_count": 1,
                    "current_count": 1,
                    "total_page": 1,
                    "current_page": 1,
                    "owners": [
                        {
                            "id": 1,
                            "email": "testchulsu@gmail.com",
                            "name": "테스트용_철수(인증X)",
                            "phone_number": "010-9776-5112",
                            "shop_id": 1,
                            "shop_name": "마슬랜 치킨",
                            "created_at" : "2024-01-15 12:00:00"
                        },
                        {
                            "id": 1,
                            "email": "testchulsu@gmail.com",
                            "name": "테스트용_철수(인증X)",
                            "phone_number": "010-9776-5112",
                            "shop_id": 2,
                            "shop_name": "신전 떡볶이",
                            "created_at" : "2024-01-15 12:00:00"
                        }
                    ]
                }
                """
            ));
    }

    @Test
    @DisplayName("관리자가 가입 신청한 사장님 리스트 조회한다 - V2")
    void getNewOwnersAdminV2() {
        for (int i = 0; i < 11; i++) {
            Owner request = Owner.builder()
                .companyRegistrationNumber("118-80-567" + i)
                .attachments(List.of(
                        OwnerAttachment.builder()
                            .url("https://test.com/사장님_인증사진_1" + i +".jpg")
                            .isDeleted(false)
                            .build(),
                        OwnerAttachment.builder()
                            .url("https://test.com/사장님_인증사진_2" + i +".jpg")
                            .isDeleted(false)
                            .build()
                    )
                )
                .grantShop(true)
                .grantEvent(true)
                .user(
                    User.builder()
                        .password(passwordEncoder.encode("1234"))
                        .nickname("사장님" + i)
                        .name("테스트용(인증X)" + i)
                        .phoneNumber("010-9776-511" + i)
                        .userType(OWNER)
                        .gender(MAN)
                        .email("testchulsu@gmail.com" + i)
                        .isAuthed(false)
                        .isDeleted(false)
                        .build()
                )
                .build();

            adminOwnerRepository.save(request);
        }

        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/admin/users/new-owners")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getInt("total_count")).isEqualTo(11);
                softly.assertThat(response.body().jsonPath().getInt("current_count")).isEqualTo(10);
                softly.assertThat(response.body().jsonPath().getInt("total_page")).isEqualTo(2);
                softly.assertThat(response.body().jsonPath().getInt("current_page")).isEqualTo(1);
                softly.assertThat(response.body().jsonPath().getList("owners").size()).isEqualTo(10);
            }
        );
    }
}
