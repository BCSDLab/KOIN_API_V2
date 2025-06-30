package in.koreatech.koin.domain.club.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.club.model.Club;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ClubRelatedKeywordResponse(
    @Schema(description = "해당 검색어의 키워드 정보 리스트")
    List<InnerKeyword> keywords
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerKeyword(
        @Schema(description = "동아리ID", example = "1", requiredMode = REQUIRED)
        Integer clubId,

        @Schema(description = "동아리명", example = "BCSD", requiredMode = REQUIRED)
        String clubName
    ) {

        public static InnerKeyword from(Club club) {
            return new InnerKeyword(club.getId(), club.getName());
        }
    }

    public static ClubRelatedKeywordResponse from(List<Club> clubs) {
        return new ClubRelatedKeywordResponse(clubs.stream()
            .map(InnerKeyword::from)
            .toList()
        );
    }
}
