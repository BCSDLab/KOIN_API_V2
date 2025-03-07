package in.koreatech.koin.domain.dining.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.dining.dto.DiningResponse;
import in.koreatech.koin.domain.dining.dto.DiningSearchResponse;
import in.koreatech.koin.domain.dining.exception.DuplicateLikeException;
import in.koreatech.koin.domain.dining.exception.LikeNotFoundException;
import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.model.DiningLikes;
import in.koreatech.koin.domain.dining.model.DiningPlace;
import in.koreatech.koin.domain.dining.repository.DiningLikesRepository;
import in.koreatech.koin.domain.dining.repository.DiningRepository;
import in.koreatech.koin._common.model.Criteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DiningService {

    private final DiningRepository diningRepository;
    private final DiningLikesRepository diningLikesRepository;

    private final Clock clock;

    public List<DiningResponse> getDinings(LocalDate date, Integer userId) {
        if (date == null) {
            date = LocalDate.now(clock);
        }
        return diningRepository.findAllByDate(date)
            .stream()
            .map(dining -> {
                boolean isLiked =
                    (userId != null) && diningLikesRepository.existsByDiningIdAndUserId(dining.getId(), userId);
                return DiningResponse.from(dining, isLiked);
            }).collect(Collectors.toList());
    }

    @Transactional(readOnly = false)
    public void likeDining(Integer userId, Integer diningId) {
        if (diningLikesRepository.existsByDiningIdAndUserId(diningId, userId)) {
            throw DuplicateLikeException.withDetail(diningId, userId);
        }

        Dining dining = diningRepository.getById(diningId);
        dining.likesDining();
        diningRepository.save(dining);

        diningLikesRepository.save(DiningLikes.builder()
            .diningId(diningId)
            .userId(userId)
            .build());
    }

    @Transactional(readOnly = false)
    public void likeDiningCancel(Integer userId, Integer diningId) {
        if (!diningLikesRepository.existsByDiningIdAndUserId(diningId, userId)) {
            throw LikeNotFoundException.withDetail(diningId, userId);
        }

        Dining dining = diningRepository.getById(diningId);
        dining.likesDiningCancel();
        diningRepository.save(dining);

        diningLikesRepository.deleteByDiningIdAndUserId(diningId, userId);
    }

    public DiningSearchResponse searchDinings(String keyword, Integer page, Integer limit, List<DiningPlace> filter) {
        Long total = diningRepository.count();
        Criteria criteria = Criteria.of(page, limit, Math.toIntExact(total));
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(),
            Sort.by(Sort.Direction.DESC, "id"));

        List<String> placeFilters = Optional.ofNullable(filter)
            .orElseGet(Collections::emptyList)
            .stream()
            .filter(Objects::nonNull)
            .map(DiningPlace::getPlaceName)
            .filter(place -> !place.isEmpty())
            .collect(Collectors.collectingAndThen(
                Collectors.toList(),
                list -> list.isEmpty() ? Arrays.stream(DiningPlace.values())
                    .map(DiningPlace::getPlaceName)
                    .collect(Collectors.toList())
                    : list
            ));

        Page<Dining> result = diningRepository.findAllByMenuContainingAndPlaceIn(
            keyword, placeFilters, pageRequest);
        return DiningSearchResponse.of(result, criteria);
    }
}
