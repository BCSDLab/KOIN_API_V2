package in.koreatech.koin.domain.timetableV3.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetableV2.dto.response.TimetableFrameResponse;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV3.model.Term;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record TimetableFramesResponseV3(
    @Schema(description = "년도", example = "2024", requiredMode = REQUIRED)
    Integer year,

    @Schema(description = "학기 별 timetableFrames", requiredMode = REQUIRED)
    List<InnerTimetableFrameResponse> timetableFrames
) {
    @JsonNaming(SnakeCaseStrategy.class)
    public record InnerTimetableFrameResponse(
        @Schema(description = "학기", example = "2학기", requiredMode = REQUIRED)
        String term,

        @Schema(description = "timetableFrame 리스트", requiredMode = REQUIRED)
        List<TimetableFrameResponse> frames
    ) {
        
    }

    public static List<TimetableFramesResponseV3> from(List<TimetableFrame> timetableFrameList) {
        List<TimetableFramesResponseV3> responseList = new ArrayList<>();
        Map<Integer, Map<Term, List<TimetableFrameResponse>>> groupedByYearAndTerm = new TreeMap<>(Comparator.reverseOrder());

        // 일차로 Term으로 그룹핑, 이후 year로 그룹핑
        for (TimetableFrame timetableFrame : timetableFrameList) {
            int year = timetableFrame.getSemester().getYear();
            Term term = timetableFrame.getSemester().getTerm();

            groupedByYearAndTerm.computeIfAbsent(year, k -> new TreeMap<>(Comparator.comparing(Term::getPriority)));

            Map<Term, List<TimetableFrameResponse>> termMap = groupedByYearAndTerm.get(year);
            termMap.computeIfAbsent(term, k -> new ArrayList<>());
            termMap.get(term).add(TimetableFrameResponse.from(timetableFrame));
        }

        // 메인 시간표가 멘 위로 그 다음은 id로 정렬
        groupedByYearAndTerm.values().forEach(termMap ->
            termMap.values().forEach(frameList ->
                frameList.sort(Comparator.comparing(TimetableFrameResponse::isMain).reversed()
                    .thenComparing(TimetableFrameResponse::id))
            )
        );

        for (Map.Entry<Integer, Map<Term, List<TimetableFrameResponse>>> yearEntry : groupedByYearAndTerm.entrySet()) {
            List<InnerTimetableFrameResponse> termResponses = new ArrayList<>();
            int year = yearEntry.getKey();
            Map<Term, List<TimetableFrameResponse>> termMap = yearEntry.getValue();

            for (Map.Entry<Term, List<TimetableFrameResponse>> termEntry : termMap.entrySet()) {
                Term term = termEntry.getKey();
                termResponses.add(new InnerTimetableFrameResponse(term.getDescription(), termEntry.getValue()));
            }
            responseList.add(new TimetableFramesResponseV3(year, termResponses));
        }

        return responseList;
    }
}
