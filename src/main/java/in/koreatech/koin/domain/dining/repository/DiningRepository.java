package in.koreatech.koin.domain.dining.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.dining.model.Dining;
import jakarta.transaction.Transactional;

public interface DiningRepository extends Repository<Dining, Long> {

    Dining save(Dining dining);

    Dining findById(Long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE dining_menus d SET d.image = :image WHERE d.id = :id", nativeQuery = true)
    void updateDiningImage(@Param("id") Long id, @Param("image") String imageUrl);

    List<Dining> findAllByDate(String date);
}
