package in.koreatech.koin.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerInVerification;
import in.koreatech.koin.domain.owner.repository.OwnerInVerificationRedisRepository;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.owner.repository.OwnerShopRedisRepository;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.fixture.ShopFixture;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SuppressWarnings("NonAsciiCharacters")
class OwnerApiTest extends AcceptanceTest {

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private ShopFixture shopFixture;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private OwnerShopRedisRepository ownerShopRedisRepository;

    @Autowired
    private OwnerInVerificationRedisRepository ownerInVerificationRedisRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("로그인된 사장님 정보를 조회한다.")
    void getOwner() {
        // given
        Owner owner = userFixture.현수_사장님();
        Shop shop = shopFixture.마슬랜(owner);
        String token = userFixture.getToken(owner.getUser());

        // when then
        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/owner")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo(String.format("""
                {
                    "email": "hysoo@naver.com",
                    "name": "테스트용_현수",
                    "company_number": "123-45-67190",
                    "attachments": [
                        {
                            "id": 1,
                            "file_url": "https://test.com/현수_사장님_인증사진_1.jpg",
                            "file_name": "현수_사장님_인증사진_1.jpg"
                        },
                        {
                            "id": 2,
                            "file_url": "https://test.com/현수_사장님_인증사진_2.jpg",
                            "file_name": "현수_사장님_인증사진_2.jpg"
                        }
                    ],
                    "shops": [
                        {
                            "id": %d,
                            "name": "마슬랜 치킨"
                        }
                    ]
                }
                """, shop.getId()
            ));
    }

    @Test
    @DisplayName("사장님이 회원가입 인증번호 전송 요청을 한다 - 전송한 코드로 인증요청이 성공한다")
    void requestAndVerifySign() {
        String ownerEmail = "junho5336@gmail.com";
        RestAssured
            .given()
            .body(String.format("""
                {
                  "address": "%s"
                }
                """, ownerEmail)
            )
            .contentType(ContentType.JSON)
            .when()
            .post("/owners/verification/email")
            .then()
            .statusCode(HttpStatus.OK.value());

        var verifyCode = ownerInVerificationRedisRepository.getByVerify(ownerEmail);

        RestAssured
            .given()
            .body(String.format("""
                    {
                      "address": "%s",
                      "certification_code": "%s"
                    }
                """, ownerEmail, verifyCode.getCertificationCode()))
            .contentType(ContentType.JSON)
            .when()
            .post("/owners/verification/code")
            .then()
            .statusCode(HttpStatus.OK.value());

        var result = ownerInVerificationRedisRepository.findById(ownerEmail);
        Assertions.assertThat(result).isNotPresent();
    }

    @Test
    @DisplayName("사장님이 회원가입 인증번호 전송 요청을 한다 - 1분 이내로 재요청시 오류가 발생한다.")
    void requestDuplicateVerifySign() {
        String ownerEmail = "junho5336@gmail.com";
        RestAssured
            .given()
            .body(String.format("""
                {
                  "address": "%s"
                }
                """, ownerEmail))
            .contentType(ContentType.JSON)
            .when()
            .post("/owners/verification/email")
            .then()
            .statusCode(HttpStatus.OK.value());
        RestAssured
            .given()
            .body(String.format("""
                {
                  "address": "%s"
                }
                """, ownerEmail))
            .contentType(ContentType.JSON)
            .when()
            .post("/owners/verification/email")
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    @DisplayName("사장님 회원가입 인증번호 전송 요청 이벤트 발생 시 슬랙 전송 이벤트가 발생한다.")
    void checkOwnerEventListener() {
        RestAssured
            .given()
            .body("""
                {
                  "address": "test@gmail.com"
                }
                """)
            .contentType(ContentType.JSON)
            .when()
            .post("/owners/verification/email")
            .then()
            .statusCode(HttpStatus.OK.value());

        verify(ownerEventListener).onOwnerEmailRequest(any());
    }

    @Nested
    @DisplayName("사장님 회원가입")
    class ownerRegister {

        @Test
        @DisplayName("사장님이 회원가입 요청을 한다.")
        void register() {
            // when & then
            var response = RestAssured
                .given()
                .body("""
                    {
                       "attachment_urls": [
                         {
                           "file_url": "https://static.koreatech.in/testimage.png"
                         }
                       ],
                       "company_number": "012-34-56789",
                       "email": "helloworld@koreatech.ac.kr",
                       "name": "최준호",
                       "password": "a0240120305812krlakdsflsa;1235",
                       "phone_number": "010-0000-0000",
                       "shop_id": null,
                       "shop_name": "기분좋은 뷔짱"
                     }
                    """)
                .contentType(ContentType.JSON)
                .when()
                .post("/owners/register")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract();

            // when
            transactionTemplate.executeWithoutResult(status -> {
                    Owner owner = ownerRepository.findByCompanyRegistrationNumber("012-34-56789").get();
                    assertSoftly(
                        softly -> {
                            softly.assertThat(owner).isNotNull();
                            softly.assertThat(owner.getUser().getName()).isEqualTo("최준호");
                            softly.assertThat(owner.getUser().getEmail()).isEqualTo("helloworld@koreatech.ac.kr");
                            softly.assertThat(owner.getCompanyRegistrationNumber()).isEqualTo("012-34-56789");
                            softly.assertThat(owner.getAttachments().size()).isEqualTo(1);
                            softly.assertThat(owner.getAttachments().get(0).getUrl())
                                .isEqualTo("https://static.koreatech.in/testimage.png");
                            softly.assertThat(owner.getUser().isAuthed()).isFalse();
                            softly.assertThat(owner.getUser().isDeleted()).isFalse();
                            verify(ownerEventListener).onOwnerRegister(any());
                        }
                    );
                }
            );
        }

        @Test
        @DisplayName("사장님이 회원가입 요청을 한다 - 첨부파일 이미지 URL이 잘못된 경우 400")
        void registerNotAllowedFileUrl() {
            // given
            RestAssured
                .given()
                .body("""
                    {
                       "attachment_urls": [
                         {
                           "file_url": "https://hello.koreatech.in/testimage.png"
                         }
                       ],
                       "company_number": "012-34-56789",
                       "email": "helloworld@koreatech.ac.kr",
                       "name": "최준호",
                       "password": "a0240120305812krlakdsflsa;1235",
                       "phone_number": "010-0000-0000",
                       "shop_id": null,
                       "shop_name": "기분좋은 뷔짱"
                     }
                    """)
                .contentType(ContentType.JSON)
                .when()
                .post("/owners/register")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("사장님이 회원가입 요청을 한다 - 잘못된 사업자 등록번호인 경우 400")
        void registerNotAllowedCompanyNumber() {
            // given
            RestAssured
                .given()
                .body("""
                    {
                       "attachment_urls": [
                         {
                           "file_url": "https://static.koreatech.in/testimage.png"
                         }
                       ],
                       "company_number": "8121-34-56789",
                       "email": "helloworld@koreatech.ac.kr",
                       "name": "최준호",
                       "password": "a0240120305812krlakdsflsa;1235",
                       "phone_number": "010-0000-0000",
                       "shop_id": null,
                       "shop_name": "기분좋은 뷔짱"
                     }
                    """)
                .contentType(ContentType.JSON)
                .when()
                .post("/owners/register")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("사장님이 회원가입 요청을 한다 - 이름이 없는경우 400")
        void registerWithoutName() {
            // given
            RestAssured
                .given()
                .body("""
                    {
                       "attachment_urls": [
                         {
                           "file_url": "https://static.koreatech.in/testimage.png"
                         }
                       ],
                       "company_number": "011-34-56789",
                       "email": "helloworld@koreatech.ac.kr",
                       "name": "",
                       "password": "a0240120305812krlakdsflsa;1235",
                       "phone_number": "010-0000-0000",
                       "shop_id": null,
                       "shop_name": "기분좋은 뷔짱"
                     }
                    """)
                .contentType(ContentType.JSON)
                .when()
                .post("/owners/register")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("사장님이 회원가입 요청을 한다 - 기존에 존재하는 상점과 함께 회원가입")
        void registerWithExistShop() {
            // given
            Shop shop = shopFixture.마슬랜(null);
            RestAssured
                .given()
                .body(String.format("""
                    {
                       "attachment_urls": [
                         {
                           "file_url": "https://static.koreatech.in/testimage.png"
                         }
                       ],
                       "company_number": "011-34-12312",
                       "email": "helloworld@koreatech.ac.kr",
                       "name": "주노",
                       "password": "a0240120305812krlakdsflsa;1235",
                       "phone_number": "010-0000-0000",
                       "shop_id": %d,
                       "shop_name": "기분좋은 뷔짱"
                     }
                    """, shop.getId()))
                .contentType(ContentType.JSON)
                .when()
                .post("/owners/register")
                .then()
                .statusCode(HttpStatus.OK.value());

            Owner owner = ownerRepository.findByCompanyRegistrationNumber("011-34-12312").get();
            var ownerShop = ownerShopRedisRepository.findById(owner.getId());
            assertSoftly(
                softly -> {
                    softly.assertThat(ownerShop).isNotNull();
                    softly.assertThat(ownerShop.getShopId()).isEqualTo(shop.getId());
                }
            );
        }

        @Test
        @DisplayName("사장님이 회원가입 요청을 한다 - 존재하지 않는 상점과 함께 회원가입")
        void registerWithNotExistShop() {
            // given
            RestAssured
                .given()
                .body("""
                    {
                       "attachment_urls": [
                         {
                           "file_url": "https://static.koreatech.in/testimage.png"
                         }
                       ],
                       "company_number": "011-34-56789",
                       "email": "helloworld@koreatech.ac.kr",
                       "name": "주노",
                       "password": "a0240120305812krlakdsflsa;1235",
                       "phone_number": "010-0000-0000",
                       "shop_id": null,
                       "shop_name": "기분좋은 뷔짱"
                     }
                    """)
                .contentType(ContentType.JSON)
                .when()
                .post("/owners/register")
                .then()
                .statusCode(HttpStatus.OK.value());
            Owner owner = ownerRepository.findByCompanyRegistrationNumber("011-34-56789").get();
            var ownerShop = ownerShopRedisRepository.findById(owner.getId());
            assertSoftly(
                softly -> {
                    softly.assertThat(ownerShop).isNotNull();
                    softly.assertThat(ownerShop.getShopId()).isNull();
                }
            );
        }
    }

    @Test
    @DisplayName("사장님이 회원가입 인증번호를 확인한다")
    void ownerCodeVerification() {
        // given
        OwnerInVerification verification = OwnerInVerification.of("junho5336@gmail.com", "123456");
        ownerInVerificationRedisRepository.save(verification);
        RestAssured
            .given()
            .body("""
                    {
                      "address": "junho5336@gmail.com",
                      "certification_code": "123456"
                    }
                """)
            .contentType(ContentType.JSON)
            .when()
            .post("/owners/verification/code")
            .then()
            .statusCode(HttpStatus.OK.value());
        var result = ownerInVerificationRedisRepository.findById(verification.getKey());
        assertThat(result).isNotPresent();
    }

    @Test
    @DisplayName("사장님이 회원가입 인증번호를 확인한다 - 존재하지 않는 이메일로 요청을 보낸다")
    void ownerCodeVerificationNotExistEmail() {
        // given
        OwnerInVerification verification = OwnerInVerification.of("junho5336@gmail.com", "123456");
        ownerInVerificationRedisRepository.save(verification);
        RestAssured
            .given()
            .body("""
                    {
                      "address": "someone@gmail.com",
                      "certification_code": "123456"
                    }
                """)
            .contentType(ContentType.JSON)
            .when()
            .post("/owners/verification/code")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("사장님이 비밀번호 변경을 위한 인증번호 이메일을 전송을 요청한다")
    void sendResetPasswordEmail() {
        // given
        String email = "test@test.com";
        RestAssured
            .given()
            .body(String.format("""
                    {
                      "address": "%s"
                    }
                """, email)
            )
            .contentType(ContentType.JSON)
            .when()
            .post("/owners/password/reset/verification")
            .then()
            .statusCode(HttpStatus.OK.value());

        assertThat(ownerInVerificationRedisRepository.findById(email)).isPresent();
    }

    @Test
    @DisplayName("사장님이 인증번호를 확인한다.")
    void ownerVerify() {
        // given
        String email = "test@test.com";
        String code = "123123";
        OwnerInVerification verification = OwnerInVerification.of(email, code);
        ownerInVerificationRedisRepository.save(verification);
        RestAssured
            .given()
            .body(String.format("""
                    {
                      "address": "%s",
                      "certification_code": "%s"
                    }
                """, email, code)
            )
            .contentType(ContentType.JSON)
            .when()
            .post("/owners/password/reset/send")
            .then()
            .statusCode(HttpStatus.OK.value());

        var result = ownerInVerificationRedisRepository.getByVerify(email);
        assertSoftly(
            softly -> {
                softly.assertThat(result).isNotNull();
                softly.assertThat(result.isAuthed()).isTrue();
            }
        );
    }

    @Test
    @DisplayName("사장님이 인증번호를 확인한다. - 중복 시 409를 반환한다.")
    void ownerVerifyDuplicated() {
        // given
        String email = "test@test.com";
        String code = "123123";
        OwnerInVerification verification = OwnerInVerification.of(email, code);
        ownerInVerificationRedisRepository.save(verification);
        // when
        RestAssured
            .given()
            .body(String.format("""
                    {
                      "address": "%s",
                      "certification_code": "%s"
                    }
                """, email, code)
            )
            .contentType(ContentType.JSON)
            .when()
            .post("/owners/password/reset/send")
            .then()
            .statusCode(HttpStatus.OK.value());

        // then
        RestAssured
            .given()
            .body(String.format("""
                    {
                      "address": "%s",
                      "certification_code": "%s"
                    }
                """, email, code)
            )
            .contentType(ContentType.JSON)
            .when()
            .post("/owners/password/reset/send")
            .then()
            .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    @DisplayName("사장님이 비밀번호를 변경한다.")
    void ownerChangePassword() {
        // given
        User user = userFixture.현수_사장님().getUser();
        String code = "123123";
        OwnerInVerification verification = OwnerInVerification.of(user.getEmail(), code);
        verification.verify();
        ownerInVerificationRedisRepository.save(verification);
        String password = "asdf1234!";

        // when
        RestAssured
            .given()
            .body(String.format("""
                    {
                       "address": "%s",
                       "password": "%s"
                     }
                """, user.getEmail(), password)
            )
            .contentType(ContentType.JSON)
            .when()
            .put("/owners/password/reset")
            .then()
            .statusCode(HttpStatus.OK.value());

        // then
        var result = ownerInVerificationRedisRepository.findById(user.getEmail());
        User userResult = userRepository.getByEmail(user.getEmail());
        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(result).isNotPresent();
                passwordEncoder.matches(password, userResult.getPassword());
            }
        );
    }

    @Test
    @DisplayName("사장님이 비밀번호를 변경한다. - 인증되지 않으면 400을 반환한다.")
    void ownerChangePasswordNotAuthed() {
        // given
        String email = "test@test.com";
        String code = "123123";
        OwnerInVerification verification = OwnerInVerification.of(email, code);
        ownerInVerificationRedisRepository.save(verification);
        String password = "asdf1234!";

        // when & then
        RestAssured
            .given()
            .body(String.format("""
                    {
                       "address": "%s",
                       "password": "%s"
                     }
                """, email, password)
            )
            .contentType(ContentType.JSON)
            .when()
            .put("/owners/password/reset")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("사장님이 회원탈퇴를 한다.")
    void ownerDelete() {
        // given
        Owner owner = userFixture.현수_사장님();
        String token = userFixture.getToken(owner.getUser());

        // when
        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .delete("/user")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract();

        // then
        assertThat(userRepository.findById(owner.getId())).isNotPresent();
    }
}
