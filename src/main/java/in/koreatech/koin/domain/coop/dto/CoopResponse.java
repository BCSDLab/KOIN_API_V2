package in.koreatech.koin.domain.coop.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record CoopResponse(
    @Schema(description = "이메일 주소", example = "koin123@koreatech.ac.kr", requiredMode = REQUIRED)
    String email,

    @Schema(description = "성별(남:0, 여:1)", example = "1", requiredMode = NOT_REQUIRED)
    Integer gender,

    @Schema(description = "이름", example = "최준호", requiredMode = NOT_REQUIRED)
    String name,

    @Schema(description = "휴대폰 번호", example = "010-0000-0000", requiredMode = NOT_REQUIRED)
    String phoneNumber,

    @Schema(description = "유저 타입", example = "COOP", requiredMode = REQUIRED)
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
