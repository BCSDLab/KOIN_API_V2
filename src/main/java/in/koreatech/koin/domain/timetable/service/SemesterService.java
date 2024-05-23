package in.koreatech.koin.domain.timetable.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetable.dto.SemesterCheckResponse;
import in.koreatech.koin.domain.timetable.dto.SemesterResponse;
import in.koreatech.koin.domain.timetable.exception.StudentSemesterNotFoundException;
import in.koreatech.koin.domain.timetable.model.TimeTable;
import in.koreatech.koin.domain.timetable.repository.SemesterRepository;
import in.koreatech.koin.domain.timetable.repository.TimeTableRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SemesterService {

    private final SemesterRepository semesterRepository;
    private final TimeTableRepository timeTableRepository;

    public List<SemesterResponse> getSemesters() {
        return semesterRepository.findAllByOrderBySemesterDesc().stream()
            .map(SemesterResponse::from)
            .toList();
    }

    public SemesterCheckResponse getCheckSemesters(Integer userId) {
        List<TimeTable> timeTables = timeTableRepository.findAllByUserId(userId);
        if (timeTables.isEmpty()) {
            throw StudentSemesterNotFoundException.withDetail("학생의 학기 정보가 없습니다.");
        }
        return SemesterCheckResponse.of(userId, timeTables);
    }
}
