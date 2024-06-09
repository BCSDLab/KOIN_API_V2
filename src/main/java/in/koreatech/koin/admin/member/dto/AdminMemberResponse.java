package in.koreatech.koin.admin.member.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.member.model.Member;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminMemberResponse(
    @Schema(description = "고유 id", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "이름", example = "김주원", requiredMode = REQUIRED)
    String name,

    @Schema(description = "학번", example = "2019136037")
    String studentNumber,

    @Schema(description = "트랙 이름", example = "BackEnd", requiredMode = REQUIRED)
    String track,

    @Schema(description = "직책", example = "Regular", requiredMode = REQUIRED)
    String position,

    @Schema(description = "이메일", example = "damiano102777@naver.com")
    String email,

    @Schema(description = "이미지 url", example = "https://static.koreatech.in/test.png")
    String imageUrl,

    @Schema(description = "삭제(soft delete) 여부", example = "false", requiredMode = REQUIRED)
    boolean isDeleted
) {
    public static AdminMemberResponse from(Member member) {
        return new AdminMemberResponse(
            member.getId(),
            member.getName(),
            member.getStudentNumber(),
            member.getTrack() != null ? member.getTrack().getName() : null,
            member.getPosition(),
            member.getEmail(),
            member.getImageUrl(),
            member.isDeleted()
        );
    }
}
