package in.koreatech.koin.admin.abtest.dto.response;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record AbtestUsersResponse(
    List<InnerUserResponse> users
) {

    public static AbtestUsersResponse from(List<User> users) {
        return new AbtestUsersResponse(users.stream().map(InnerUserResponse::from).toList());
    }

    @JsonNaming(SnakeCaseStrategy.class)
    private record InnerUserResponse(
        @Schema(description = "사용자 ID", example = "1")
        Integer id,

        @Schema(description = "사용자 이름", example = "홍길동")
        String name,

        @Schema(description = "사용자 상세 정보(전화번호 or 이메일)", example = "010-1234-5678")
        String detail
    ) {

        public static InnerUserResponse from(User user) {
            if (user.getUserType().equals(UserType.OWNER)) {
                return new InnerUserResponse(user.getId(), user.getName(), user.getPhoneNumber());
            }
            return new InnerUserResponse(user.getId(), user.getName(), user.getEmail());
        }
    }
}
