package in.koreatech.koin.domain.dining.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.dining.model.DiningLikes;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface DiningLikesRepository extends Repository<DiningLikes, Integer> {

    Boolean existsByDiningIdAndUserId(Integer diningId, Integer userId);

    DiningLikes save(DiningLikes diningLikes);

    void deleteByDiningIdAndUserId(Integer diningId, Integer userId);
}
