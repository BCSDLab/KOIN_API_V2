package in.koreatech.koin.domain.dining.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.dining.dto.DiningLikeRequest;
import in.koreatech.koin.domain.dining.dto.DiningResponse;
import in.koreatech.koin.domain.dining.exception.DuplicateLikeException;
import in.koreatech.koin.domain.dining.exception.LikeNotFoundException;
import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.model.DiningLikes;
import in.koreatech.koin.domain.dining.repository.DiningLikesRepository;
import in.koreatech.koin.domain.dining.repository.DiningRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiningService {

    private final DiningRepository diningRepository;
    private final DiningLikesRepository diningLikesRepository;

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

    @Transactional(readOnly = false)
    public void likeDining(DiningLikeRequest diningLikeRequest) {
        if (diningLikesRepository.existsByDiningIdAndUserId(diningLikeRequest.diningId(), diningLikeRequest.userId())) {
            throw DuplicateLikeException.withDetail(diningLikeRequest.diningId(), diningLikeRequest.userId());
        }

        Dining dining = diningRepository.getById(diningLikeRequest.diningId());
        dining.likesDining();
        diningRepository.save(dining);

        diningLikesRepository.save(DiningLikes.builder()
            .diningId(diningLikeRequest.diningId())
            .userId(diningLikeRequest.userId())
            .build());
    }

    @Transactional(readOnly = false)
    public void likeDiningCancel(DiningLikeRequest diningLikeRequest) {
        if (!diningLikesRepository.existsByDiningIdAndUserId(diningLikeRequest.diningId(),
            diningLikeRequest.userId())) {
            throw LikeNotFoundException.withDetail(diningLikeRequest.diningId(), diningLikeRequest.userId());
        }

        Dining dining = diningRepository.getById(diningLikeRequest.diningId());
        dining.likesDiningCancel();
        diningRepository.save(dining);

        diningLikesRepository.deleteByDiningIdAndUserId(
            diningLikeRequest.diningId(),
            diningLikeRequest.userId()
        );
    }
}
