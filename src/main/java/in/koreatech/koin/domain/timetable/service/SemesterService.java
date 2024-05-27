package in.koreatech.koin.domain.timetable.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetable.dto.SemesterCheckResponse;
import in.koreatech.koin.domain.timetable.dto.SemesterResponse;
import in.koreatech.koin.domain.timetable.model.TimeTable;
import in.koreatech.koin.domain.timetable.repository.SemesterRepository;
import in.koreatech.koin.domain.timetable.repository.TimeTableRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SemesterService {

    private final SemesterRepository semesterRepository;
    private final TimeTableRepository timetableRepository;

    public List<SemesterResponse> getSemesters() {
        return semesterRepository.findAllByOrderBySemesterDesc().stream()
            .map(SemesterResponse::from)
            .toList();
    }

    public SemesterCheckResponse getStudentSemesters(Integer userId) {
        List<TimeTable> timetables = timetableRepository.findAllByUserId(userId);
        List<String> semesters = timetables.stream()
            .map(timetable -> timetable.getSemester().getSemester())
            .distinct()
            .toList();
        return SemesterCheckResponse.of(userId, semesters);
    }
}
