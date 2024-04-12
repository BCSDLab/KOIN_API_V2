package in.koreatech.koin.domain.user.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CoopResponse(
    @Schema(description = "이메일 주소", example = "koin123@koreatech.ac.kr")
    String email,

    @Schema(description = "성별(남:0, 여:1)", example = "1")
    Integer gender,

    @Schema(description = "이름", example = "최준호")
    String name,

    @Schema(description = "휴대폰 번호", example = "010-0000-0000")
    String phoneNumber,

    @Schema(description = "유저 타입", example = "COOP")
    String userType
) {

    public static CoopResponse from(User user) {
        Integer userGender = null;
        if (user.getGender() != null) {
            userGender = user.getGender().ordinal();
        }
        return new CoopResponse(
            user.getEmail(),
            userGender,
            user.getName(),
            user.getPhoneNumber(),
            user.getUserType().getValue()
        );
    }
}
