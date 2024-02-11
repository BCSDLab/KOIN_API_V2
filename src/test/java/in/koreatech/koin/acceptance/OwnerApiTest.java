package in.koreatech.koin.acceptance;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.ArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.owner.domain.Owner;
import in.koreatech.koin.domain.owner.domain.OwnerAttachment;
import in.koreatech.koin.domain.owner.repository.OwnerAttachmentRepository;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.repository.ShopRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class OwnerApiTest extends AcceptanceTest {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private OwnerAttachmentRepository ownerAttachmentRepository;

    @Autowired
    private JwtProvider jwtProvider;

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

        Shop shop = Shop.builder()
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

        OwnerAttachment attachmentRequest = OwnerAttachment.builder()
            .url("https://test.com/test.jpg")
            .isDeleted(false)
            .build();

        Owner ownerRequest = Owner.builder()
            .companyRegistrationNumber("123-45-67890")
            .companyRegistrationCertificateImageUrl("https://test.com/test.jpg")
            .grantShop(true)
            .grantEvent(true)
            .attachments(new ArrayList<>())
            .shops(new ArrayList<>())
            .user(user)
            .build();
        attachmentRequest.setOwner(ownerRequest);
        shop.setOwner(ownerRequest);

        Owner owner = ownerRepository.save(ownerRequest);
        OwnerAttachment attachment = ownerAttachmentRepository.findById(attachmentRequest.getId());
        String token = jwtProvider.createToken(ownerRequest.getUser());

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
                softly.assertThat(response.body().jsonPath().getString("company_number")).isEqualTo(owner.getCompanyRegistrationNumber());

                softly.assertThat(response.body().jsonPath().getLong("attachments[0].id")).isEqualTo(attachment.getId().intValue());
                softly.assertThat(response.body().jsonPath().getString("attachments[0].file_url")).isEqualTo(attachment.getUrl());
                softly.assertThat(response.body().jsonPath().getString("attachments[0].file_name")).isEqualTo(attachment.getName());

                softly.assertThat(response.body().jsonPath().getLong("shops[0].id")).isEqualTo(shop.getId().intValue());
                softly.assertThat(response.body().jsonPath().getString("shops[0].name")).isEqualTo(shop.getName());

                softly.assertThat(response.body().jsonPath().getList("attachments").size()).isEqualTo(1);
                softly.assertThat(response.body().jsonPath().getList("shops").size()).isEqualTo(1);
            }
        );
    }
}
