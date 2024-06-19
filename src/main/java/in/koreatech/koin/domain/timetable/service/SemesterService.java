package in.koreatech.koin.domain.timetable.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetable.dto.SemesterCheckResponse;
import in.koreatech.koin.domain.timetable.dto.SemesterResponse;
import in.koreatech.koin.domain.timetable.model.TimetableFrame;
import in.koreatech.koin.domain.timetable.repository.SemesterRepository;
import in.koreatech.koin.domain.timetable.repository.TimetableFrameRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SemesterService {

    private final SemesterRepository semesterRepository;
    private final TimetableFrameRepository timetableFrameRepository;

    public List<SemesterResponse> getSemesters() {
        return semesterRepository.findAllByOrderBySemesterDesc().stream()
            .map(SemesterResponse::from)
            .toList();
    }

    public SemesterCheckResponse getStudentSemesters(Integer userId) {
        List<TimetableFrame> timeTableFrames = timetableFrameRepository.findByUserIdAndIsMainTrue(userId);
        List<String> semesters = timeTableFrames.stream()
            .map(timeTableFrame -> timeTableFrame.getSemester().getSemester())
            .distinct()
            .toList();
        return SemesterCheckResponse.of(userId, semesters);
    }
}
