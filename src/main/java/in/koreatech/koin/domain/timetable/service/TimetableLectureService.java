package in.koreatech.koin.domain.timetable.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetable.repository.TimeTableLectureRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableLectureService {

    private final TimeTableLectureRepository timeTableLectureRepository;

    @Transactional
    public void deleteTimetableLecture(Integer id) {
        timeTableLectureRepository.getById(id);
        timeTableLectureRepository.deleteById(id);
    }
}
