package in.koreatech.koin.domain.timetable.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import in.koreatech.koin.domain.timetable.model.Semester;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetable.dto.SemesterCheckResponse;
import in.koreatech.koin.domain.timetable.dto.SemesterResponse;
import in.koreatech.koin.domain.timetable.repository.SemesterRepository;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SemesterService {

    private final SemesterRepository semesterRepository;
    private final TimetableFrameRepositoryV2 timetableFrameRepositoryV2;

    public List<SemesterResponse> getSemesters() {
        return semesterRepository.findAllByOrderBySemesterDesc().stream()
                .sorted(Comparator.comparing(this::customSemesterSort).reversed())
                .map(SemesterResponse::from)
                .toList();
    }

    public SemesterCheckResponse getStudentSemesters(Integer userId) {
        List<TimetableFrame> timetableFrames = timetableFrameRepositoryV2.findByUserIdAndIsMainTrue(userId);
        List<Semester> semesters = timetableFrames.stream()
                .map(TimetableFrame::getSemester)
                .distinct()
                .sorted(Comparator.comparing(this::customSemesterSort).reversed())
                .toList();
        return SemesterCheckResponse.of(userId, semesters);
    }

    private String customSemesterSort(Semester semester) {
        String semesterValue = semester.getSemester();
        if (semesterValue.contains("-")) {
            String[] parts = semesterValue.split("-");
            return parts[0] + getSeasonValueForSorted(parts[1]);
        }
        return semesterValue;
    }

    private String getSeasonValueForSorted(String season) {
        if (Objects.equals(season, "겨울")) {
            return "21";
        } return "11";
    }
}
