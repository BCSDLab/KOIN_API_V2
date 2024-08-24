package in.koreatech.koin.domain.community.keywords.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(SnakeCaseStrategy.class)
public record KeywordNotificationRequest (
    @Schema(description = "업데이트 된 공지사항 목록", example = "[1, 2, 3]")
    @NotNull
    List<Integer> updateNotification
) {

}
