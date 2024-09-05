package in.koreatech.koin.admin.acceptance;

import static in.koreatech.koin.domain.user.model.UserGender.MAN;
import static in.koreatech.koin.domain.user.model.UserIdentity.UNDERGRADUATE;
import static in.koreatech.koin.domain.user.model.UserType.OWNER;
import static org.assertj.core.api.Assertions.assertThat;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.ArrayList;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.admin.user.repository.AdminOwnerRepository;
import in.koreatech.koin.admin.user.repository.AdminOwnerShopRedisRepository;
import in.koreatech.koin.admin.user.repository.AdminStudentRepository;
import in.koreatech.koin.admin.user.repository.AdminUserRepository;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerAttachment;
import in.koreatech.koin.domain.owner.model.OwnerShop;
import in.koreatech.koin.domain.owner.repository.OwnerShopRedisRepository;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.fixture.ShopFixture;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.support.JsonAssertions;

@SuppressWarnings("NonAsciiCharacters")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class AdminUserApiTest extends AcceptanceTest {

    @Autowired
    private AdminStudentRepository adminStudentRepository;

    @Autowired
    private AdminOwnerRepository adminOwnerRepository;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private AdminOwnerShopRedisRepository adminOwnerShopRedisRepository;

    @Autowired
    private OwnerShopRedisRepository ownerShopRedisRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private ShopFixture shopFixture;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeAll
    void setup() {
        clear();
    }

    @Test
    void 관리자가_학생_리스트를_파라미터가_없이_조회한다_페이지네이션() throws Exception {
        Student student = userFixture.준호_학생();
        User adminUser = userFixture.코인_운영자();

        String token = userFixture.getToken(adminUser);

        mockMvc.perform(
                get("/admin/students")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                     "current_count": 1,
                     "current_page": 1,
                     "students": [
                         {
                             "email": "juno@koreatech.ac.kr",
                             "id": 1,
                             "major": "컴퓨터공학부",
                             "name": "테스트용_준호",
                             "nickname": "준호",
                             "student_number": "2019136135"
                         }
                     ],
                     "total_count": 1,
                     "total_page": 1
                 }
                """));
    }

    @Test
    void 관리자가_학생_리스트를_페이지_수와_limits으로_조회한다_페이지네이션() throws Exception {
        for (int i = 0; i < 11; i++) {
            Student student = Student.builder()
                .studentNumber("2019136135")
                .anonymousNickname("익명" + i)
                .department("컴퓨터공학부")
                .userIdentity(UNDERGRADUATE)
                .isGraduated(false)
                .user(
                    User.builder()
                        .password(passwordEncoder.encode("1234"))
                        .nickname("성재" + i)
                        .name("테스트용_성재" + i)
                        .phoneNumber("01012345670")
                        .userType(STUDENT)
                        .gender(MAN)
                        .email("seongjae@koreatech.ac.kr")
                        .isAuthed(true)
                        .isDeleted(false)
                        .build()
                )
                .build();

            adminStudentRepository.save(student);
        }

        User adminUser = userFixture.코인_운영자();

        String token = userFixture.getToken(adminUser);

        mockMvc.perform(
                get("/admin/students")
                    .header("Authorization", "Bearer " + token)
                    .param("page", "2")
                    .param("limit", "10")
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                     "current_count": 1,
                     "current_page": 2,
                     "students": [
                         {
                             "email": "seongjae@koreatech.ac.kr",
                             "id": 11,
                             "major": "컴퓨터공학부",
                             "name": "테스트용_성재10",
                             "nickname": "성재10",
                             "student_number": "2019136135"
                         }
                     ],
                     "total_count": 11,
                     "total_page": 2
                 }
                """));
    }

    @Test
    void 관리자가_학생_리스트를_닉네임으로_조회한다_페이지네이션() throws Exception {
        Student student1 = userFixture.성빈_학생();
        Student student2 = userFixture.준호_학생();
        User adminUser = userFixture.코인_운영자();

        String token = userFixture.getToken(adminUser);

        mockMvc.perform(
                get("/admin/students")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("nickname", "준호")
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                     "current_count": 1,
                     "current_page": 1,
                     "students": [
                         {
                             "email": "juno@koreatech.ac.kr",
                             "id": 2,
                             "major": "컴퓨터공학부",
                             "name": "테스트용_준호",
                             "nickname": "준호",
                             "student_number": "2019136135"
                         }
                     ],
                     "total_count": 1,
                     "total_page": 1
                 }
                """));
    }

    @Test
    void 관리자가_로그인_한다() throws Exception {
        User adminUser = userFixture.코인_운영자();
        String email = adminUser.getEmail();
        String password = "1234";

        mockMvc.perform(
                post("/admin/user/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "email" : "%s",
                          "password" : "%s"
                        }
                        """.formatted(email, password))
            )
            .andExpect(status().isCreated());
    }

    @Test
    void 관리자가_로그인_한다_관리자가_아니면_404_반환() throws Exception {
        Student student = userFixture.준호_학생();
        String email = student.getUser().getEmail();
        String password = "1234";

        mockMvc.perform(
                post("/admin/user/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "email" : "%s",
                          "password" : "%s"
                        }
                        """.formatted(email, password))
            )
            .andExpect(status().isNotFound());
    }

    @Test
    void 관리자가_로그아웃한다() throws Exception {
        User adminUser = userFixture.코인_운영자();

        String token = userFixture.getToken(adminUser);

        mockMvc.perform(
                post("/admin/user/logout")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
    }

    @Test
    void 관리자가_액세스_토큰_재발급_한다() throws Exception {
        User adminUser = userFixture.코인_운영자();
        String email = adminUser.getEmail();
        String password = "1234";

        String token = userFixture.getToken(adminUser);

        MvcResult result = mockMvc.perform(
                post("/admin/user/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "email" : "%s",
                          "password" : "%s"
                        }
                        """.formatted(email, password))
            )
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode loginJsonNode = JsonAssertions.convertJsonNode(result);

        mockMvc.perform(
                post("/admin/user/refresh")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "refresh_token" : "%s"
                        }
                        """.formatted(loginJsonNode.get("refresh_token").asText()))
            )
            .andExpect(status().isCreated());
    }

    @Test
    void 관리자가_사장님_권한_요청을_허용한다() throws Exception {
        Owner owner = userFixture.철수_사장님();
        Shop shop = shopFixture.마슬랜(null);

        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        OwnerShop ownerShop = OwnerShop.builder()
            .ownerId(owner.getId())
            .shopId(shop.getId())
            .build();

        ownerShopRedisRepository.save(ownerShop);

        mockMvc.perform(
                put("/admin/owner/{id}/authed", owner.getUser().getId())
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk());

        //영속성 컨테스트 동기화
        Owner updatedOwner = adminOwnerRepository.getById(owner.getId());
        var resultOwnerShop = adminOwnerShopRedisRepository.findById(owner.getId());

        assertSoftly(
            softly -> {
                softly.assertThat(updatedOwner.getUser().isAuthed()).isEqualTo(true);
                softly.assertThat(updatedOwner.isGrantShop()).isEqualTo(true);
                softly.assertThat(resultOwnerShop).isEmpty();
            }
        );
    }

    @Test
    void 관리자가_특정_학생_정보를_조회한다_관리자가_아니면_403_반환() throws Exception {
        Student student = userFixture.준호_학생();
        String token = userFixture.getToken(student.getUser());

        mockMvc.perform(
                get("/admin/users/student/{id}", student.getUser().getId())
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void 관리자가_특정_학생_정보를_조회한다() throws Exception {
        Student student = userFixture.준호_학생();

        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        mockMvc.perform(
                get("/admin/users/student/{id}", student.getUser().getId())
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "anonymous_nickname": "익명",
                    "created_at": "2024-01-15 12:00:00",
                    "email": "juno@koreatech.ac.kr",
                    "gender": 0,
                    "id": 1,
                    "is_authed": true,
                    "is_graduated": false,
                    "last_logged_at": null,
                    "major": "컴퓨터공학부",
                    "name": "테스트용_준호",
                    "nickname": "준호",
                    "phone_number": "01012345678",
                    "student_number": "2019136135",
                    "updated_at": "2024-01-15 12:00:00",
                    "user_type": "STUDENT"
                }
                """));
    }

    @Test
    void 관리자가_특정_학생_정보를_수정한다() throws Exception {
        Student student = userFixture.준호_학생();

        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        mockMvc.perform(
                put("/admin/users/student/{id}", student.getUser().getId())
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                          {
                            "gender" : 1,
                            "major" : "기계공학부",
                            "name" : "서정빈",
                            "password" : "0c4be6acaba1839d3433c1ccf04e1eec4d1fa841ee37cb019addc269e8bc1b77",
                            "nickname" : "duehee",
                            "phone_number" : "01023456789",
                            "student_number" : "2019136136"
                          }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "anonymous_nickname": "익명",
                    "email": "juno@koreatech.ac.kr",
                    "gender": 1,
                    "major": "기계공학부",
                    "name": "서정빈",
                    "nickname": "duehee",
                    "phone_number": "01023456789",
                    "student_number": "2019136136"
                }
                """));

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
    }

    @Test
    void 관리자가_특정_사장을_조회한다() throws Exception {
        Owner owner = userFixture.현수_사장님();
        Shop shop = shopFixture.마슬랜(owner);

        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        mockMvc.perform(
                get("/admin/users/owner/{id}", owner.getUser().getId())
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                {
                    "id": 1,
                    "email": "hysoo@naver.com",
                    "name": "테스트용_현수",
                    "nickname": "현수",
                    "company_registration_number": "123-45-67190",
                    "attachments_url": [
                        "https://test.com/현수_사장님_인증사진_1.jpg",
                        "https://test.com/현수_사장님_인증사진_2.jpg"
                    ],
                    "shops_id": [
                        %d
                    ],
                    "phone_number": "01098765432",
                    "is_authed": true,
                    "user_type": "OWNER",
                    "gender": 0,
                    "created_at" : "2024-01-15 12:00:00",
                    "updated_at" : "2024-01-15 12:00:00",
                    "last_logged_at" : null
                }
                """, shop.getId())));
    }

    @Test
    void 관리자가_특정_사장을_수정한다() throws Exception {
        Owner owner = userFixture.현수_사장님();
        Shop shop = shopFixture.마슬랜(owner);

        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        mockMvc.perform(
                put("/admin/users/owner/{id}", owner.getUser().getId())
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                          {
                            "company_registration_number" : "123-45-67190",
                            "grant_shop" : "false",
                            "grant_event" : "false"
                          }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "company_registration_number" : "123-45-67190",
                    "email" : "hysoo@naver.com",
                    "gender" : 0,
                    "grant_shop" : false,
                    "grant_event" : false,
                    "name" : "테스트용_현수",
                    "nickname" : "현수",
                    "phone_number" : "01098765432"
                }
                """));
    }

    @Test
    void 관리자가_가입_신청한_사장님_리스트_조회한다() throws Exception {
        Owner owner = userFixture.철수_사장님();
        Shop shop = shopFixture.마슬랜(null);

        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        OwnerShop ownerShop = OwnerShop.builder()
            .ownerId(owner.getId())
            .shopId(shop.getId())
            .build();

        ownerShopRedisRepository.save(ownerShop);

        mockMvc.perform(
                get("/admin/users/new-owners")
                    .header("Authorization", "Bearer " + token)
                    .param("searchType", "NAME")
                    .param("query", "철수")
                    .param("sort", "CREATED_AT_DESC")
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
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
                            "phone_number": "01097765112",
                            "shop_id": 1,
                            "shop_name": "마슬랜 치킨",
                            "created_at" : "2024-01-15 12:00:00"
                        }
                    ]
                }
                """));
    }

    @Test
    void 관리자가_가입_신청한_사장님_리스트_조회한다_V2() throws Exception {
        for (int i = 0; i < 11; i++) {
            User user = User.builder()
                .password(passwordEncoder.encode("1234"))
                .nickname("사장님" + i)
                .name("테스트용(인증X)" + i)
                .phoneNumber("0109776511" + i)
                .userType(OWNER)
                .gender(MAN)
                .email("testchulsu@gmail.com" + i)
                .isAuthed(false)
                .isDeleted(false)
                .build();

            Owner owner = Owner.builder()
                .user(user)
                .companyRegistrationNumber("118-80-567" + i)
                .grantShop(true)
                .grantEvent(true)
                .attachments(new ArrayList<>())
                .build();

            OwnerAttachment attachment1 = OwnerAttachment.builder()
                .url("https://test.com/사장님_인증사진_1" + i + ".jpg")
                .isDeleted(false)
                .owner(owner)
                .build();

            OwnerAttachment attachment2 = OwnerAttachment.builder()
                .url("https://test.com/사장님_인증사진_2" + i + ".jpg")
                .isDeleted(false)
                .owner(owner)
                .build();

            owner.getAttachments().add(attachment1);
            owner.getAttachments().add(attachment2);

            adminOwnerRepository.save(owner);
        }

        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        mockMvc.perform(
                get("/admin/users/new-owners")
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total_count").value(11))
            .andExpect(jsonPath("$.current_count").value(10))
            .andExpect(jsonPath("$.total_page").value(2))
            .andExpect(jsonPath("$.current_page").value(1))
            .andExpect(jsonPath("$.owners.length()").value(10));
    }

    @Test
    void 관리자가_가입_사장님_리스트_조회한다() throws Exception {
        for (int i = 0; i < 11; i++) {
            User user = User.builder()
                .password(passwordEncoder.encode("1234"))
                .nickname("사장님" + i)
                .name("테스트용(인증X)" + i)
                .phoneNumber("0109776511" + i)
                .userType(OWNER)
                .gender(MAN)
                .email("testchulsu@gmail.com" + i)
                .isAuthed(true)
                .isDeleted(false)
                .build();

            Owner owner = Owner.builder()
                .user(user)
                .companyRegistrationNumber("118-80-567" + i)
                .grantShop(true)
                .grantEvent(true)
                .attachments(new ArrayList<>())
                .build();

            OwnerAttachment attachment1 = OwnerAttachment.builder()
                .url("https://test.com/사장님_인증사진_1" + i + ".jpg")
                .isDeleted(false)
                .owner(owner)
                .build();

            OwnerAttachment attachment2 = OwnerAttachment.builder()
                .url("https://test.com/사장님_인증사진_2" + i + ".jpg")
                .isDeleted(false)
                .owner(owner)
                .build();

            owner.getAttachments().add(attachment1);
            owner.getAttachments().add(attachment2);

            adminOwnerRepository.save(owner);
        }

        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        mockMvc.perform(
                get("/admin/users/owners")
                    .header("Authorization", "Bearer " + token)

            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total_count").value(11))
            .andExpect(jsonPath("$.current_count").value(10))
            .andExpect(jsonPath("$.total_page").value(2))
            .andExpect(jsonPath("$.current_page").value(1))
            .andExpect(jsonPath("$.owners.length()").value(10));
    }

    @Test
    void 관리자가_회원을_조회한다() throws Exception {
        Student student = userFixture.준호_학생();

        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        mockMvc.perform(
                get("/admin/users/{id}", student.getUser().getId())
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nickname").value("준호"))
            .andExpect(jsonPath("$.name").value("테스트용_준호"))
            .andExpect(jsonPath("$.phoneNumber").value("01012345678"))
            .andExpect(jsonPath("$.email").value("juno@koreatech.ac.kr"));
    }

    @Test
    void 관리자가_회원을_삭제한다() throws Exception {
        Student student = userFixture.준호_학생();

        User adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser);

        mockMvc.perform(
                delete("/admin/users/{id}", student.getUser().getId())
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        assertThat(adminUserRepository.findById(student.getId())).isNotPresent();
    }
}
