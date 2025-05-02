package in.koreatech.koin.domain.user.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record UserResponse(
    @Schema(example = "1", description = "학생 고유 id")
    Integer id,

    @Schema(example = "example12", description = "일반인 로그인 id")
    String loginId,

    @Schema(description = "이메일 주소", example = "koin123@koreatech.ac.kr")
    String email,

    @Schema(description = "성별(남:0, 여:1)", example = "1")
    Integer gender,

    @Schema(description = "이름", example = "최준호")
    String name,

    @Schema(description = "닉네임", example = "juno")
    String nickname,

    @Schema(description = "휴대폰 번호", example = "010-0000-0000")
    String phoneNumber,

    @Schema(description = "사용자 타입", example = "GENERAL")
    UserType userType
) {

    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getUserId(),
            user.getEmail(),
            user.getGender() != null ? user.getGender().ordinal() : null,
            user.getName(),
            user.getNickname(),
            user.getPhoneNumber(),
            user.getUserType()
        );
    }
}
