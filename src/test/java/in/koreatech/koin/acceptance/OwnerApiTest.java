package in.koreatech.koin.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerShop;
import in.koreatech.koin.domain.owner.model.redis.OwnerVerificationStatus;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.owner.repository.OwnerShopRedisRepository;
import in.koreatech.koin.domain.owner.repository.redis.OwnerVerificationStatusRepository;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.fixture.ShopFixture;
import in.koreatech.koin.fixture.UserFixture;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
    private OwnerVerificationStatusRepository ownerVerificationStatusRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void 사장님이_로그인을_진행한다() throws Exception {
        Owner owner = userFixture.원경_사장님();
        String phoneNumber = owner.getAccount();
        String password = "1234";

        mockMvc.perform(
                post("/owner/login")
                    .content("""
                        {
                          "account" : "%s",
                          "password" : "%s"
                        }
                        """.formatted(phoneNumber, password))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated());
    }

    @Test
    void 관리자가_승인하지_않은_사장님이_로그인을_진행한다() throws Exception {
        Owner owner = userFixture.철수_사장님();
        String phoneNumber = owner.getAccount();
        String password = "1234";

        mockMvc.perform(
                post("/owner/login")
                    .content("""
                        {
                          "account" : "%s",
                          "password" : "%s"
                        }
                        """.formatted(phoneNumber, password))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void 로그인된_사장님_정보를_조회한다() throws Exception {
        // given
        Owner owner = userFixture.현수_사장님();
        Shop shop = shopFixture.마슬랜(owner, null);
        String token = userFixture.getToken(owner.getUser());

        mockMvc.perform(
                get("/owner")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                    {
                      "email": "hysoo@naver.com",
                      "name": "테스트용_현수",
                      "company_number": "123-45-67190",
                      "account" : "01098765432",
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
                              "id": 1,
                              "name": "마슬랜 치킨"
                          }
                      ]
                  }
                """));
    }

    @Test
    void 사장님이_회원가입_인증번호_전송_요청을_한다_전송한_코드로_인증요청이_성공한다() throws Exception {
        String ownerEmail = "junho5336@gmail.com";

        mockMvc.perform(
                post("/owners/verification/email")
                    .content(String.format("""
                        {
                          "address": "%s"
                        }
                        """, ownerEmail))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        var verifyCode = ownerVerificationStatusRepository.getByVerify(ownerEmail);

        mockMvc.perform(
                post("/owners/verification/code")
                    .content(String.format("""
                            {
                              "address": "%s",
                              "certification_code": "%s"
                            }
                        """, ownerEmail, verifyCode.getCertificationCode()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
        var result = ownerVerificationStatusRepository.findById(ownerEmail);
        Assertions.assertThat(result).isNotPresent();
    }

    @Test
    void 사장님_회원가입_이메일_인증번호_전송_요청_이벤트_발생_시_슬랙_전송_이벤트가_발생한다() throws Exception {
        mockMvc.perform(
                post("/owners/verification/email")
                    .content("""
                            {
                              "address": "test@gmail.com"
                            }
                        """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
        forceVerify(() -> verify(ownerEventListener).onOwnerEmailRequest(any()));
        clear();
    }

    @Nested
    @DisplayName("사장님 회원가입")
    @Transactional
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ownerRegister {

        @Test
        void 사장님이_이메일로_회원가입_요청을_한다() throws Exception {
            // when & then
            mockMvc.perform(
                    post("/owners/register")
                        .content("""
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
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

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
                            forceVerify(() -> verify(ownerEventListener).onOwnerRegisterBySms(any()));
                            clear();
                        }
                    );
                }
            );
        }

        @Test
        void 사장님이_전화번호를_아이디로_회원가입_요청을_한다() throws Exception {
            // when & then
            mockMvc.perform(
                    post("/owners/register/phone")
                        .content("""
                                {
                                   "attachment_urls": [
                                     {
                                       "file_url": "https://static.koreatech.in/testimage.png"
                                     }
                                   ],
                                   "company_number": "012-34-56789",
                                   "name": "최준호",
                                   "password": "a0240120305812krlakdsflsa;1235",
                                   "phone_number": "01012341234",
                                   "shop_number": "0510000000",
                                   "shop_name": "기분좋은 뷔짱"
                                }
                            """)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

            // when
            transactionTemplate.executeWithoutResult(status -> {
                    Owner owner = ownerRepository.findByCompanyRegistrationNumber("012-34-56789").get();
                    assertSoftly(
                        softly -> {
                            softly.assertThat(owner).isNotNull();
                            softly.assertThat(owner.getUser().getName()).isEqualTo("최준호");
                            softly.assertThat(owner.getUser().getEmail()).isEqualTo(null);
                            softly.assertThat(owner.getUser().getPhoneNumber()).isEqualTo("01012341234");
                            softly.assertThat(owner.getCompanyRegistrationNumber()).isEqualTo("012-34-56789");
                            softly.assertThat(owner.getAccount()).isEqualTo("01012341234");
                            softly.assertThat(owner.getAttachments().size()).isEqualTo(1);
                            softly.assertThat(owner.getAttachments().get(0).getUrl())
                                .isEqualTo("https://static.koreatech.in/testimage.png");
                            softly.assertThat(owner.getUser().isAuthed()).isFalse();
                            softly.assertThat(owner.getUser().isDeleted()).isFalse();
                            forceVerify(() -> verify(ownerEventListener).onOwnerRegisterBySms(any()));
                            clear();
                        }
                    );
                }
            );
        }

        @Test
        void 사장님이_회원가입_요청을_한다_첨부파일_이미지_URL이_잘못된_경우_400() throws Exception {
            // given
            mockMvc.perform(
                    post("/owners/register")
                        .content("""
                                {
                                   "attachment_urls": [
                                     {
                                       "file_url": "https://hello.koreatech.in/testimage.png"
                                     }
                                   ],
                                   "company_number": "012-34-56789",
                                   "account": "helloworld@koreatech.ac.kr",
                                   "name": "최준호",
                                   "password": "a0240120305812krlakdsflsa;1235",
                                   "phone_number": "010-0000-0000",
                                   "shop_id": null,
                                   "shop_name": "기분좋은 뷔짱"
                                 }
                            """)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        void 사장님이_회원가입_요청을_한다_잘못된_사업자_등록번호인_경우_400() throws Exception {
            // given
            mockMvc.perform(
                    post("/owners/register")
                        .content("""
                                {
                                   "attachment_urls": [
                                     {
                                       "file_url": "https://static.koreatech.in/testimage.png"
                                     }
                                   ],
                                   "company_number": "8121-34-56789",
                                   "account": "helloworld@koreatech.ac.kr",
                                   "name": "최준호",
                                   "password": "a0240120305812krlakdsflsa;1235",
                                   "phone_number": "01000000000",
                                   "shop_id": null,
                                   "shop_name": "기분좋은 뷔짱"
                                }
                            """)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        void 사장님이_회원가입_요청을_한다_이름이_없는경우_400() throws Exception {
            // given
            mockMvc.perform(
                    post("/owners/register")
                        .content("""
                                {
                                   "attachment_urls": [
                                     {
                                       "file_url": "https://static.koreatech.in/testimage.png"
                                     }
                                   ],
                                   "company_number": "011-34-56789",
                                   "account": "helloworld@koreatech.ac.kr",
                                   "name": "",
                                   "password": "a0240120305812krlakdsflsa;1235",
                                   "phone_number": "01000000000",
                                   "shop_id": null,
                                   "shop_name": "기분좋은 뷔짱"
                                 }
                            """)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        void 사장님이_회원가입_요청을_한다_기존에_존재하는_상점과_함께_회원가입() throws Exception {
            // given
            Shop shop = shopFixture.마슬랜(null, null);
            mockMvc.perform(
                    post("/owners/register")
                        .content(String.format("""
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
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

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
        void 사장님이_회원가입_요청을_한다_존재하지_않는_상점과_함께_회원가입() throws Exception {
            // given
            mockMvc.perform(
                    post("/owners/register")
                        .content("""
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
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
            Owner owner = ownerRepository.findByCompanyRegistrationNumber("011-34-56789").get();
            var ownerShop = ownerShopRedisRepository.findById(owner.getId());
            assertSoftly(
                softly -> {
                    softly.assertThat(ownerShop).isNotNull();
                    softly.assertThat(ownerShop.getShopId()).isNull();
                }
            );
        }

        @Test
        void 사장님이_전화번호를_아이디로_회원가입을_했을_때_저장된_가게정보를_조회한다() throws Exception {
            Owner owner = userFixture.현수_사장님();
            String token = userFixture.getToken(owner.getUser());

            OwnerShop ownerShop = OwnerShop.builder()
                .ownerId(owner.getId())
                .shopName("가게명")
                .shopNumber("01012345678")
                .build();

            ownerShopRedisRepository.save(ownerShop);

            mockMvc.perform(
                    get("/owner/registered-store")
                        .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                    {
                        "shopName": "가게명",
                        "shopNumber": "01012345678"
                    }
                    """));
        }
    }

    @Test
    void 사장님이_회원가입_인증번호를_확인한다() throws Exception {
        // given
        OwnerVerificationStatus verification = OwnerVerificationStatus.of("junho5336@gmail.com", "123456");
        ownerVerificationStatusRepository.save(verification);
        mockMvc.perform(
                post("/owners/verification/code")
                    .content("""
                            {
                              "address": "junho5336@gmail.com",
                              "certification_code": "123456"
                            }
                        """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
        var result = ownerVerificationStatusRepository.findById(verification.getKey());
        assertThat(result).isNotPresent();
    }

    @Test
    void 사장님이_회원가입_확인한다_존재하지_않는_이메일로_요청을_보낸다() throws Exception {
        // given
        OwnerVerificationStatus verification = OwnerVerificationStatus.of("junho5336@gmail.com", "123456");
        ownerVerificationStatusRepository.save(verification);
        mockMvc.perform(
                post("/owners/verification/code")
                    .content("""
                        {
                          "address": "someone@gmail.com",
                          "certification_code": "123456"
                        }
                        """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNotFound());
    }

    @Test
    void 사장님이_비밀번호_변경을_위한_인증번호_이메일을_전송을_요청한다() throws Exception {
        // given
        Owner owner = userFixture.현수_사장님();
        ownerRepository.save(owner);
        mockMvc.perform(
                post("/owners/password/reset/verification")
                    .content(String.format("""
                        {   
                          "address": "%s"
                        }
                        """, owner.getUser().getEmail()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
        assertThat(ownerVerificationStatusRepository.findById(owner.getUser().getEmail())).isPresent();
    }

    @Test
    void 사장님이_비밀번호_변경을_위한_인증번호_이메일을_전송을_하루_요청_횟수_5번_를_초과하여_요청한다_400에러를_반환한다() throws Exception {
        // given
        int DAILY_LIMIT = 5;
        Owner owner = userFixture.현수_사장님();
        ownerRepository.save(owner);
        for (int i = 0; i < DAILY_LIMIT; ++i) {
            mockMvc.perform(
                    post("/owners/password/reset/verification")
                        .content(String.format("""
                                {
                                  "address": "%s"
                                }
                            """, owner.getUser().getEmail()))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
        }
        mockMvc.perform(
                post("/owners/password/reset/verification")
                    .content(String.format("""
                            {
                              "address": "%s"
                            }
                        """, owner.getUser().getEmail()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 사장님이_인증번호를_확인한다() throws Exception {
        // given
        String email = "test@test.com";
        String code = "123123";
        OwnerVerificationStatus verification = OwnerVerificationStatus.of(email, code);
        ownerVerificationStatusRepository.save(verification);

        mockMvc.perform(
                post("/owners/password/reset/send")
                    .content(String.format("""
                        {
                          "address": "%s",
                          "certification_code": "%s"
                        }
                        """, email, code))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        var result = ownerVerificationStatusRepository.findById(email);
        assertSoftly(
            softly -> {
                softly.assertThat(result).isNotNull();
                softly.assertThat(result).isNotPresent();
            }
        );
    }

    @Test
    void 사장님이_인증번호를_확인한다_중복_시_404를_반환한다() throws Exception {
        // given
        String email = "test@test.com";
        String code = "123123";
        OwnerVerificationStatus verification = OwnerVerificationStatus.of(email, code);
        ownerVerificationStatusRepository.save(verification);
        // when
        mockMvc.perform(
                post("/owners/password/reset/send")
                    .content(String.format("""
                        {
                          "address": "%s",
                          "certification_code": "%s"
                        }
                        """, email, code))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        mockMvc.perform(
                post("/owners/password/reset/send")
                    .content(String.format("""
                        {
                          "address": "%s",
                          "certification_code": "%s"
                        }
                        """, email, code))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNotFound());
    }

    @Test
    void 사장님이_비밀번호를_변경한다() throws Exception {
        // given
        User user = userFixture.현수_사장님().getUser();
        String code = "123123";
        OwnerVerificationStatus verification = OwnerVerificationStatus.of(user.getEmail(), code);
        ownerVerificationStatusRepository.save(verification);
        String password = "asdf1234!";

        mockMvc.perform(
                post("/owners/password/reset/send")
                    .content(String.format("""
                        {
                          "address": "%s",
                          "certification_code": "%s"
                        }
                        """, user.getEmail(), code))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        mockMvc.perform(
                put("/owners/password/reset")
                    .content(String.format("""
                            {
                               "address": "%s",
                               "password": "%s"
                             }
                        """, user.getEmail(), password))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
        // then
        var result = ownerVerificationStatusRepository.findById(user.getEmail());
        User userResult = userRepository.getByEmail(user.getEmail());
        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(result).isNotPresent();
                passwordEncoder.matches(password, userResult.getPassword());
            }
        );
    }

    @Test
    void 사장님이_회원탈퇴를_한다() throws Exception {
        // given
        Owner owner = userFixture.현수_사장님();
        String token = userFixture.getToken(owner.getUser());

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/user")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());
        // then
        assertThat(userRepository.findById(owner.getId())).isNotPresent();
    }

    @Test
    void 사업자_등록번호_중복_검증_존재하지_않으면_200() throws Exception {
        // when & then
        mockMvc.perform(
                get("/owners/exists/company-number")
                    .queryParam("company_number", "123-45-67190")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
    }

    @Test
    void 사업자_등록번호_중복_검증_이미_존재하면_409() throws Exception {
        // given
        Owner owner = userFixture.현수_사장님();
        // when & then
        mockMvc.perform(
                get("/owners/exists/company-number")
                    .queryParam("company_number", owner.getCompanyRegistrationNumber())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isConflict());
    }

    @Test
    void 사업자_등록번호_중복_검증_값이_존재하지_않으면_400() throws Exception {
        // when & then
        mockMvc.perform(
                get("/owners/exists/company-number")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 사업자_등록번호_중복_검증_값이_올바르지_않으면_400() throws Exception {
        // when & then
        mockMvc.perform(
                get("/owners/exists/company-number")
                    .queryParam("company_number", "1234567890")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 사장님_아이디_전화번호_중복_검증_존재하지_않으면_200() throws Exception {
        mockMvc.perform(
                get("/owners/exists/account")
                    .param("account", "01012345678")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
    }

    @Test
    void 사장님_아이디_전화번호_중복_검증_이미_존재하면_409() throws Exception {
        Owner owner = userFixture.현수_사장님();
        mockMvc.perform(
                get("/owners/exists/account")
                    .param("account", owner.getAccount())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isConflict());
    }

    @Test
    void 사장님_아이디_전화번호_중복_검증_파라미터에_전화번호를_포함하지_않으면_400() throws Exception {
        mockMvc.perform(
                get("/owners/exists/account")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 사장님_아이디_전화번호_중복_검증_잘못된_전화번호_형식이면_400() throws Exception {
        String phoneNumber = "123123123123";
        mockMvc.perform(
                get("/owners/exists/account")
                    .param("phone_number", phoneNumber)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }
}
