package in.koreatech.koin.domain.dining.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.dining.model.DiningReport;
import in.koreatech.koin.domain.dining.model.DiningReportChange;
import in.koreatech.koin.domain.dining.model.DiningReportSequence;
import in.koreatech.koin.domain.dining.repository.DiningReportChangeRepository;
import in.koreatech.koin.domain.dining.repository.DiningReportSequenceRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiningReportChangeService {

    private final DiningReportSequenceRepository sequenceRepository;
    private final DiningReportChangeRepository changeRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void append(List<DiningReport> reports, DiningReportChange.EventType eventType, LocalDateTime now) {
        if (reports.isEmpty()) {
            return;
        }
        // Acquire this last and retain it until the business transaction commits.
        DiningReportSequence sequence = Objects.requireNonNull(sequenceRepository.findForUpdate(),
            "식단 제보 변경 순번 초기값이 없습니다.");
        for (DiningReport report : reports) {
            changeRepository.save(DiningReportChange.from(sequence.next(), report, eventType, now));
        }
    }
}
