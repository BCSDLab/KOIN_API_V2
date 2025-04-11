package in.koreatech.koin.admin.user.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminsResponse(
    @Schema(description = "조건에 해당하는 어드민 계정 수", example = "10", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "조건에 해당하는 어드민 계정 중 현재 페이지에서 조회된 수", example = "5", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "조건에 해당하는 어드민 계정을 조회할 수 있는 최대 페이지", example = "2", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "1", requiredMode = REQUIRED)
    Integer currentPage,

    @Schema(description = "어드민 계정 리스트", requiredMode = REQUIRED)
    List<InnerAdminsResponse> admins
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerAdminsResponse(
        @Schema(description = "고유 id", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "이메일", example = "koin00001@koreatech.ac.kr", requiredMode = REQUIRED)
        String email,

        @Schema(description = "이름", example = "신관규", requiredMode = REQUIRED)
        String name,

        @Schema(description = "트랙 이름", example = "Backend", requiredMode = REQUIRED)
        String trackName,

        @Schema(description = "팀 이름", example = "User", requiredMode = REQUIRED)
        String teamName
    ) {
        public static InnerAdminsResponse from(Admin admin) {
            User user = admin.getUser();

            return new InnerAdminsResponse(
                admin.getId(),
                user.getEmail(),
                user.getName(),
                admin.getTrackType().getValue(),
                admin.getTeamType().getValue()
            );
        }
    }

    public static AdminsResponse of(Page<Admin> adminsPage) {
        return new AdminsResponse(
            adminsPage.getTotalElements(),
            adminsPage.getContent().size(),
            adminsPage.getTotalPages(),
            adminsPage.getNumber() + 1,
            adminsPage.getContent().stream()
                .map(InnerAdminsResponse::from)
                .collect(Collectors.toList())
        );
    }
}
