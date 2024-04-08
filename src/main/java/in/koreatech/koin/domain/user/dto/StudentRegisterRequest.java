package in.koreatech.koin.domain.user.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.user.model.UserIdentity;
import in.koreatech.koin.domain.user.model.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record StudentRegisterRequest(
    @Schema(description = "이메일", example = "koin123@koreatech.ac.kr")
    @Email(message = "이메일 형식을 지켜주세요.")
    @NotBlank(message = "이메일을 입력해주세요.")
    String email,

    @Schema(description = "이름", example = "최준호")
    @Size(max = 50, message = "이름은 50자 이내여야 합니다.")
    String name,

    @Schema(description = " SHA 256 해시 알고리즘으로 암호화된 비밀번호", example = "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    String password,

    @Schema(description = "닉네임", example = "bbo")
    @Size(max = 10, message = "닉네임은 최대 10자입니다.")
    String nickname,

    @Schema(description = "성별(남:0, 여:1)", example = "0")
    UserGender gender,

    @Schema(description = "졸업 여부", example = "false")
    Boolean isGraduated,

    @Schema(description = "전공{기계공학부, 컴퓨터공학부, 메카트로닉스공학부, 전기전자통신공학부, 디자인공학부, 건축공학부, 화학생명공학부, 에너지신소재공학부, 산업경영학부, 고용서비스정책학과}", example = "컴퓨터공학부")
    @JsonProperty("major")
    String department,

    @Schema(description = "학번", example = "2021136012")
    @Size(min = 10, max = 10, message = "학번은 10자여야합니다.")
    String studentNumber,

    @Schema(description = "휴대폰 번호", example = "010-0000-0000")
    @Pattern(regexp = "^[0-9]{3}-[0-9]{3,4}-[0-9]{4}", message = "전화번호 형식이 올바르지 않습니다.")
    String phoneNumber
) {
    public Student toStudent(PasswordEncoder passwordEncoder, Clock clock) {
        User user = User.builder()
            .password(passwordEncoder.encode(password))
            .email(email)
            .name(name)
            .nickname(nickname)
            .gender(gender)
            .phoneNumber(phoneNumber)
            .isAuthed(false)
            .isDeleted(false)
            .userType(UserType.STUDENT)
            .authToken(UUID.randomUUID().toString())
            .authExpiredAt(LocalDateTime.now(clock).plusHours(1))
            .build();

        return Student.builder()
            .user(user)
            .anonymousNickname("익명_" + (System.currentTimeMillis()))
            .isGraduated(isGraduated)
            .userIdentity(UserIdentity.UNDERGRADUATE)
            .department(department)
            .studentNumber(studentNumber)
            .build();
    }
}
