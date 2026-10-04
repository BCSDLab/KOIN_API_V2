package in.koreatech.koin.domain.dining.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.dining.model.DiningReportChange;
import in.koreatech.koin.domain.dining.model.DiningReportProcessingType;
import in.koreatech.koin.domain.dining.model.DiningReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record DiningReportChangesResponse(
    @Schema(description = "커밋된 제보 변경 목록. sequence 오름차순이며 변경이 없으면 빈 배열입니다.", requiredMode = REQUIRED)
    List<Change> changes,
    @Schema(description = "마지막 반환 sequence의 숫자 문자열. 변경이 없으면 요청 cursor를 유지합니다.",
        example = "2", type = "string", pattern = "^[0-9]{1,19}$", requiredMode = REQUIRED)
    String nextCursor,
    @Schema(description = "이번 응답에 이어 조회할 변경이 있는지 여부. true이면 next_cursor로 이어서 조회합니다.",
        example = "false", requiredMode = REQUIRED)
    boolean hasMore
) {

    @JsonNaming(SnakeCaseStrategy.class)
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Change(
        @Schema(description = "변경 순번의 숫자 문자열. 커밋된 변경을 이 순서로 조회합니다.", example = "2",
            type = "string", pattern = "^[1-9][0-9]{0,18}$", requiredMode = REQUIRED)
        String sequence,
        @Schema(description = "변경된 제보 ID", example = "1", requiredMode = REQUIRED)
        Integer reportId,
        @Schema(description = "변경 종류. CREATED는 접수, PROCESSED는 승인·반려 처리입니다.",
            example = "PROCESSED", requiredMode = REQUIRED)
        DiningReportChange.EventType eventType,
        @Schema(description = "해당 변경 시점의 제보 상태", example = "APPROVED", requiredMode = REQUIRED)
        DiningReportStatus status,
        @Schema(description = "해당 변경 시점의 처리 유형. CREATED이면 null입니다.", example = "MANUAL",
            nullable = true, requiredMode = REQUIRED)
        DiningReportProcessingType processingType,
        @Schema(description = "처리 묶음 UUID. CREATED이면 null입니다.",
            example = "550e8400-e29b-41d4-a716-446655440000", type = "string", format = "uuid",
            nullable = true, requiredMode = REQUIRED)
        UUID processingId,
        @Schema(description = "변경 발생 시각 (한국시간)", example = "2026-10-02 12:01:00",
            implementation = String.class, type = "string", pattern = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$",
            requiredMode = REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime occurredAt
    ) {
        public static Change from(DiningReportChange change) {
            return new Change(change.getSequence().toString(), change.getReportId(), change.getEventType(),
                change.getStatus(), change.getProcessingType(), change.getProcessingId(), change.getOccurredAt());
        }
    }
}
