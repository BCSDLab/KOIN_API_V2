package in.koreatech.koin.admin.member.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.member.model.Member;
import in.koreatech.koin._common.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminMembersResponse(
    @Schema(description = "조건에 해당하는 총 회원의 수", example = "57", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "조건에 해당하는 회원 중에 현재 페이지에서 조회된 수", example = "10", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "조건에 해당하는 회원들을 조회할 수 있는 최대 페이지", example = "6", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "2", requiredMode = REQUIRED)
    Integer currentPage,

    @Schema(description = "회원 정보 리스트", requiredMode = REQUIRED)
    List<SimpleMemberInformation> members
) {
    public static AdminMembersResponse of(Page<Member> pagedResult, Criteria criteria) {
        return new AdminMembersResponse(
            pagedResult.getTotalElements(),
            pagedResult.getContent().size(),
            pagedResult.getTotalPages(),
            criteria.getPage() + 1,
            pagedResult.getContent()
                .stream()
                .map(SimpleMemberInformation::from)
                .collect(Collectors.toList())
        );
    }
    @JsonNaming(value = SnakeCaseStrategy.class)
    private record SimpleMemberInformation(
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

        public static SimpleMemberInformation from(Member member) {
            return new SimpleMemberInformation(
                member.getId(),
                member.getName(),
                member.getStudentNumber(),
                member.getTrack().getName(),
                member.getPosition(),
                member.getEmail(),
                member.getImageUrl(),
                member.isDeleted()
            );
        }
    }
}
