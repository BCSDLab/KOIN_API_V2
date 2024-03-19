package in.koreatech.koin.controller;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import static in.koreatech.koin.domain.user.model.UserType.OWNER;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.domain.upload.ImageUploadDomain;
import in.koreatech.koin.global.domain.upload.UploadService;
import in.koreatech.koin.global.domain.upload.UploadUrlRequest;
import in.koreatech.koin.global.domain.upload.UploadUrlResponse;
import io.restassured.RestAssured;
import static io.restassured.http.ContentType.JSON;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UploadControllerTest extends AcceptanceTest {

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private UploadService service;

    @Test
    void Presigned_url_요청_시_요청_형식과_응답_형식이_올바르다() {
        // given
        when(clock.instant()).thenReturn(
            ZonedDateTime.parse("2024-02-21 18:00:00 KST", ofPattern("yyyy-MM-dd " + "HH:mm:ss z")).toInstant());
        when(clock.getZone()).thenReturn(Clock.systemDefaultZone().getZone());
        when(service.getPresignedUrl(any(ImageUploadDomain.class), any(UploadUrlRequest.class)))
            .thenReturn(
                new UploadUrlResponse(
                    "https://presigned-url",
                    "fileUrl",
                    LocalDateTime.of(2024, 2, 21, 18, 0, 0))
            );

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

        userRepository.save(user);
        String token = jwtProvider.createToken(user);

        // when
        var response = RestAssured
            .given()
            .body("""
                {
                  "content_length": 100000,
                  "content_type": "image/png",
                  "file_name": "apple.png"
                }
                """)
            .header("Authorization", "Bearer " + token)
            .contentType(JSON)
            .when()
            .post("/owners/upload/url")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        // then
        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getString("pre_signed_url"))
                    .isEqualTo("https://presigned-url");
                softly.assertThat(response.body().jsonPath().getString("file_url"))
                    .isEqualTo("fileUrl");
                softly.assertThat(response.body().jsonPath().getString("expiration_date"))
                    .isEqualTo("2024-02-21 18:00:00");
            }
        );
    }
}
