package in.koreatech.koin.domain.dining.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    public List<DiningResponse> getDinings(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return diningRepository.findAllByDate(date.format(DateTimeFormatter.ISO_DATE))
            .stream()
            .map(DiningResponse::from)
            .toList();
    }
}
