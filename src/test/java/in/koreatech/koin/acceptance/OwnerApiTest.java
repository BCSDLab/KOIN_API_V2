package in.koreatech.koin.acceptance;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerAttachment;
import in.koreatech.koin.domain.owner.model.OwnerInVerification;
import in.koreatech.koin.domain.owner.model.OwnerRegisterEvent;
import in.koreatech.koin.domain.owner.repository.OwnerInVerificationRedisRepository;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.owner.repository.OwnerShopRedisRepository;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.repository.ShopRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.global.auth.JwtProvider;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class OwnerApiTest extends AcceptanceTest {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private OwnerInVerificationRedisRepository ownerInVerificationRedisRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private OwnerShopRedisRepository ownerShopRedisRepository;

    @Test
    @DisplayName("로그인된 사장님 정보를 조회한다.")
    void getOwner() {
        // given
        User user = User.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(OWNER)
            .gender(UserGender.MAN)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        OwnerAttachment attachment = OwnerAttachment.builder()
            .url("https://test.com/test.jpg")
            .isDeleted(false)
            .build();

        Owner ownerRequest = Owner.builder()
            .companyRegistrationNumber("123-45-67890")
            .attachments(List.of(attachment))
            .grantShop(true)
            .grantEvent(true)
            .user(user)
            .build();
        Owner owner = ownerRepository.save(ownerRequest);

        Shop shopRequest = Shop.builder()
            .owner(owner)
            .name("테스트 상점")
            .internalName("테스트")
            .chosung("테스트")
            .phone("010-1234-5678")
            .address("대전광역시 유성구 대학로 291")
            .description("테스트 상점입니다.")
            .delivery(true)
            .deliveryPrice(3000L)
            .payCard(true)
            .payBank(true)
            .isDeleted(false)
            .isEvent(false)
            .remarks("비고")
            .hit(0L)
            .build();
        Shop shop = shopRepository.save(shopRequest);
        String token = jwtProvider.createToken(owner.getUser());

        // when then
        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/owner")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getString("email")).isEqualTo(user.getEmail());
                softly.assertThat(response.body().jsonPath().getString("name")).isEqualTo(user.getName());
                softly.assertThat(response.body().jsonPath().getString("company_number"))
                    .isEqualTo(owner.getCompanyRegistrationNumber());

                softly.assertThat(response.body().jsonPath().getLong("attachments[0].id"))
                    .isEqualTo(attachment.getId().intValue());
                softly.assertThat(response.body().jsonPath().getString("attachments[0].file_url"))
                    .isEqualTo(attachment.getUrl());
                softly.assertThat(response.body().jsonPath().getString("attachments[0].file_name"))
                    .isEqualTo(attachment.getName());

                softly.assertThat(response.body().jsonPath().getLong("shops[0].id")).isEqualTo(shop.getId().intValue());
                softly.assertThat(response.body().jsonPath().getString("shops[0].name")).isEqualTo(shop.getName());

                softly.assertThat(response.body().jsonPath().getList("attachments").size()).isEqualTo(1);
                softly.assertThat(response.body().jsonPath().getList("shops").size()).isEqualTo(1);
            }
        );
    }

    @Test
    @DisplayName("사장님이 회원가입 인증번호 전송 요청을 한다. - 슬랙 전송 실패해도 200으로 응답")
    void requestSignUpEmailVerification() {
        RestAssured
            .given()
            .body("""
                {
                  "email": "test@gmail.com"
                }
                """)
            .contentType(ContentType.JSON)
            .when()
            .post("/owners/verification/email")
            .then()
            .statusCode(HttpStatus.OK.value());

        assertDoesNotThrow(() ->
            ownerInVerificationRedisRepository.getByEmail("test@gmail.com")
        );
    }

    @Test
    @DisplayName("사장님이 회원가입 인증번호 전송 요청을 한다 - 전송한 코드로 인증요청이 성공한다")
    void requestAndVerifySign() {
        String ownerEmail = "junho5336@gmail.com";
        RestAssured
            .given()
            .body(String.format("""
                {
                  "email": "%s"
                }
                """, ownerEmail))
            .contentType(ContentType.JSON)
            .when()
            .post("/owners/verification/email")
            .then()
            .statusCode(HttpStatus.OK.value());
        var verify = ownerInVerificationRedisRepository.getByEmail(ownerEmail);
        RestAssured
            .given()
            .body(String.format("""
                    {
                      "address": "%s",
                      "certification_code": "%s"
                    }
                """, ownerEmail, verify.getCertificationCode()))
            .contentType(ContentType.JSON)
            .when()
            .post("/owners/verification/code")
            .then()
            .statusCode(HttpStatus.OK.value());
        var result = ownerInVerificationRedisRepository.findById(ownerEmail);
        Assertions.assertThat(result).isNotPresent();
    }

    @Test
    @DisplayName("사장님 회원가입 인증번호 전송 요청 이벤트 발생 시 슬랙 전송 이벤트가 발생한다.")
    void checkOwnerEventListener() {
        RestAssured
            .given()
            .body("""
                {
                  "email": "test@gmail.com"
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
                       "company_number": "012-34-56789",
                       "email": "helloworld@koreatech.ac.kr",
                       "name": "최준호",
                       "password": "a0240120305812krlakdsflsa;1235",
                       "phone_number": "010-0000-0000",
                       "shop_id": 0,
                       "shop_name": "기분좋은 뷔짱"
                     }
                    """)
                .contentType(ContentType.JSON)
                .when()
                .post("/owners/register")
                .then()
                .statusCode(HttpStatus.OK.value());

            // when
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    Owner owner = ownerRepository.findByCompanyRegistrationNumber("012-34-56789").get();
                    var event = new OwnerRegisterEvent(owner);
                    assertSoftly(
                        softly -> {
                            softly.assertThat(owner).isNotNull();
                            softly.assertThat(owner.getUser().getName()).isEqualTo("최준호");
                            softly.assertThat(owner.getUser().getEmail()).isEqualTo("helloworld@koreatech.ac.kr");
                            softly.assertThat(owner.getCompanyRegistrationNumber()).isEqualTo("012-34-56789");
                            softly.assertThat(owner.getAttachments().size()).isEqualTo(1);
                            softly.assertThat(owner.getAttachments().get(0).getUrl())
                                .isEqualTo("https://static.koreatech.in/testimage.png");
                            softly.assertThat(owner.getUser().getIsAuthed()).isFalse();
                            softly.assertThat(owner.getUser().getIsDeleted()).isFalse();
                            verify(ownerEventListener).onOwnerRegister(any());
                        }
                    );
                }
            });
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
                       "shop_id": 0,
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
                       "shop_id": 0,
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
                       "shop_id": 0,
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
            Shop shopRequest = Shop.builder()
                .name("테스트 상점")
                .internalName("테스트")
                .chosung("테스트")
                .phone("010-1234-56778")
                .address("대전광역시 유성구 대학로 291")
                .description("테스트 상점입니다.")
                .delivery(true)
                .deliveryPrice(3000L)
                .payCard(true)
                .payBank(true)
                .isDeleted(false)
                .isEvent(false)
                .remarks("비고")
                .hit(0L)
                .build();

            var shop = shopRepository.save(shopRequest);

            RestAssured
                .given()
                .body("""
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
                       "shop_id": 1,
                       "shop_name": "기분좋은 뷔짱"
                     }
                    """)
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
                       "shop_id": 0,
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
        var result = ownerInVerificationRedisRepository.findById(verification.getEmail());
        Assertions.assertThat(result).isNotPresent();
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
}
