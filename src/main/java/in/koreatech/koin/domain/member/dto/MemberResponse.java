package in.koreatech.koin.domain.member.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.member.model.Member;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record MemberResponse(
    @Schema(example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(example = "최준호", requiredMode = REQUIRED)
    String name,

    @Schema(example = "2019136135", requiredMode = NOT_REQUIRED)
    String studentNumber,

    @Schema(example = "Backend", requiredMode = NOT_REQUIRED)
    String track,

    @Schema(example = "Regular", requiredMode = REQUIRED)
    String position,

    @Schema(example = "juno@gmail.com", requiredMode = NOT_REQUIRED)
    String email,

    @Schema(example = "https://static.koreatech.in/bcsdlab_page_assets/img/people/juno.jpg", requiredMode = NOT_REQUIRED)
    String imageUrl,

    @Schema(example = "false", requiredMode = REQUIRED)
    boolean isDeleted,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 일자", example = "2020-08-14 16:26:35", requiredMode = REQUIRED)
    LocalDateTime createdAt,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "수정 일자", example = "2021-08-16 06:42:44", requiredMode = REQUIRED)
    LocalDateTime updatedAt
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
            member.getId(),
            member.getName(),
            member.getStudentNumber(),
            member.getTrack().getName(),
            member.getPosition(),
            member.getEmail(),
            member.getImageUrl(),
            member.isDeleted(),
            member.getCreatedAt(),
            member.getUpdatedAt()
        );
    }
}
