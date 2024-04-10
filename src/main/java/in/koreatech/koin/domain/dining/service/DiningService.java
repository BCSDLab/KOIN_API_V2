package in.koreatech.koin.domain.dining.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.dining.dto.DiningResponse;
import in.koreatech.koin.domain.dining.repository.DiningRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiningService {

    private final DiningRepository diningRepository;

    private final Clock clock;

    public List<DiningResponse> getDinings(LocalDate date) {
        if (date == null) {
            date = LocalDate.now(clock);
        }
        return diningRepository.findAllByDate(date)
            .stream()
            .map(DiningResponse::from)
            .toList();
    }
}
