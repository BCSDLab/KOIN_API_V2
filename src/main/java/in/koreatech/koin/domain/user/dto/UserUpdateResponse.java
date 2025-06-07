package in.koreatech.koin.domain.user.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record UserUpdateResponse(
    @Schema(description = "성별(남:0, 여:1)", example = "1")
    Integer gender,

    @Schema(description = "이메일 주소", example = "general123@naver.com")
    String email,

    @Schema(description = "이름", example = "최준호")
    String name,

    @Schema(description = "닉네임", example = "juno")
    String nickname,

    @Schema(description = "휴대폰 번호", example = "01000000000")
    String phoneNumber
) {

    public static UserUpdateResponse from(User user) {
        return new UserUpdateResponse(
            user.getGender() != null ? user.getGender().ordinal() : null,
            user.getEmail(),
            user.getName(),
            user.getNickname(),
            user.getPhoneNumber()
        );
    }
}
