package in.koreatech.koin.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record StudentRegisterRequest(
    @Schema(description = "이메일", example = "koin123@koreatech.ac.kr")
    @Email(message = "이메일 형식을 지켜주세요.")
    @NotBlank(message = "이메일을 입력해주세요.")
    String email,

    @Schema(description = "이름")
    @Size(max = 50, message = "이름은 50자 이내여야 합니다.")
    String name,

    @Schema(description = """
        SHA 256 해시 알고리즘으로 암호화된 비밀번호
        example: asdf1234!
        """, example = "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    String password,

    @Schema(description = "닉네임", example = "bbo")
    @Size(max = 10, message = "닉네임은 최대 10자입니다.")
    String nickname,

    @Schema(description = "성별(남:0, 여:1)", example = "0")
    UserGender gender,

    @Schema(description = "졸업 여부", example = "false")
    Boolean isGraduated,

    @Schema(description = "전공{기계공학부, 컴퓨터공학부, 메카트로닉스공학부, 전기전자통신공학부, 디자인건축공학부, 에너지신소재화학공학부, 산업경영학부}", example = "컴퓨터공학부")
    @JsonProperty("major")
    String department,

    @Schema(description = "학번", example = "2029136012")
    @Size(max = 50, message = "학번은 50자 이내여야 합니다.")
    String studentNumber,

    @Schema(description = "휴대폰 번호", example = "010-0000-0000")
    @Pattern(regexp = "^[0-9]{3}-[0-9]{3,4}-[0-9]{4}", message = "전화번호 형식이 올바르지 않습니다.")
    String phoneNumber
) {
    public static Student toStudent(StudentRegisterRequest request){
        User user = User.builder()
            .password(request.password)
            .email(request.email)
            .name(request.name)
            .nickname(request.nickname)
            .gender(request.gender)
            .phoneNumber(request.phoneNumber)
            .build();

        return Student.builder()
            .user(user)
            .isGraduated(request.isGraduated)
            .department(request.department)
            .studentNumber(request.studentNumber)
            .build();
    }
}
