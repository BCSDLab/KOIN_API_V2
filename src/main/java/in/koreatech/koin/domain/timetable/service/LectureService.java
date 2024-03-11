package in.koreatech.koin.domain.timetable.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetable.dto.LectureResponse;
import in.koreatech.koin.domain.timetable.exception.SemesterNotFoundException;
import in.koreatech.koin.domain.timetable.repository.LectureRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LectureService {
    private final LectureRepository lectureRepository;

    public List<LectureResponse> getLecturesBySemester(String semester) {

        if(!lectureRepository.existsBySemester(semester)){
            throw SemesterNotFoundException.withDetail(semester + "는 ");
        }

        return lectureRepository.findBySemester(semester)
            .stream()
            .map(LectureResponse::from)
            .toList();
    }
}
