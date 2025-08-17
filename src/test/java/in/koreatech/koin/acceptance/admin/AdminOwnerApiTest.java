package in.koreatech.koin.acceptance.admin;

import static in.koreatech.koin.domain.user.model.UserGender.MAN;
import static in.koreatech.koin.domain.user.model.UserType.OWNER;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.ShopAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.admin.owner.repository.AdminOwnerRepository;
import in.koreatech.koin.admin.owner.repository.AdminOwnerShopRedisRepository;
import in.koreatech.koin.admin.operators.model.Admin;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerAttachment;
import in.koreatech.koin.domain.owner.model.OwnerShop;
import in.koreatech.koin.domain.owner.repository.OwnerShopRedisRepository;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.user.model.User;

public class AdminOwnerApiTest extends AcceptanceTest {

    @Autowired
    private AdminOwnerRepository adminOwnerRepository;

    @Autowired
    private AdminOwnerShopRedisRepository adminOwnerShopRedisRepository;

    @Autowired
    private OwnerShopRedisRepository ownerShopRedisRepository;

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private ShopAcceptanceFixture shopFixture;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeAll
    void setup() {
        clear();
    }

    @Test
    void 관리자가_사장님_권한_요청을_허용한다() throws Exception {
        Owner owner = userFixture.철수_사장님();
        Shop shop = shopFixture.마슬랜(null, null);

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

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

        assertSoftly(
            softly -> {
                softly.assertThat(updatedOwner.getUser().isAuthed()).isEqualTo(true);
                softly.assertThat(updatedOwner.isGrantShop()).isEqualTo(true);
            }
        );
    }

    @Test
    void 관리자가_특정_사장을_조회한다() throws Exception {
        Owner owner = userFixture.현수_사장님();
        Shop shop = shopFixture.마슬랜(owner, null);

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

        mockMvc.perform(
                get("/admin/users/owner/{id}", owner.getUser().getId())
                    .header("Authorization", "Bearer " + token)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                {
                    "id": 1,
                    "email": null,
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
        Shop shop = shopFixture.마슬랜(owner, null);

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

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
                    "email" : null,
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
        Shop shop = shopFixture.마슬랜(null, null);

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

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
                .loginPw(passwordEncoder.encode("1234"))
                .nickname("사장님" + i)
                .name("테스트용(인증X)" + i)
                .userType(OWNER)
                .gender(MAN)
                .email("testchulsu@gmail.com" + i)
                .loginId("test" + i)
                .isAuthed(false)
                .isDeleted(false)
                .build();

            Owner owner = Owner.builder()
                .user(user)
                .account(String.format("0101234%04d", i))
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

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

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
                .loginPw(passwordEncoder.encode("1234"))
                .nickname("사장님" + i)
                .name("테스트용(인증X)" + i)
                .userType(OWNER)
                .gender(MAN)
                .email("testchulsu@gmail.com" + i)
                .loginId("testchulsu" + i)
                .isAuthed(true)
                .isDeleted(false)
                .build();

            Owner owner = Owner.builder()
                .user(user)
                .account(String.format("0101234%04d", i))
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

        Admin adminUser = userFixture.코인_운영자();
        String token = userFixture.getToken(adminUser.getUser());

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
}
