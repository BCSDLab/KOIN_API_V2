package in.koreatech.koin.domain.TimeTable.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.TimeTable.dto.SemesterResponse;
import in.koreatech.koin.domain.TimeTable.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SemesterService {

    private final SemesterRepository semesterRepository;

    public List<SemesterResponse> getSemesters() {
        return semesterRepository.findAll().stream()
            .map(SemesterResponse::from).toList();
    }
}
