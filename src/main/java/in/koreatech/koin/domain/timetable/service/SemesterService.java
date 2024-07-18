package in.koreatech.koin.domain.timetable.service;

import java.util.Comparator;
import java.util.List;

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
            .map(SemesterResponse::from)
            .toList();
    }

    public SemesterCheckResponse getStudentSemesters(Integer userId) {
        List<TimetableFrame> timetableFrames = timetableFrameRepositoryV2.findByUserIdAndIsMainTrue(userId);
        List<String> semesters = timetableFrames.stream()
            .map(timetableFrame -> timetableFrame.getSemester().getSemester())
            .distinct()
            .sorted(Comparator.reverseOrder())
            .toList();
        return SemesterCheckResponse.of(userId, semesters);
    }
}
