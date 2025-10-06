package in.koreatech.koin.domain.activity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.activity.model.Activity;
import in.koreatech.koin.global.marker.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface ActivityRepository extends Repository<Activity, Integer> {

    @Query(value = "SELECT * FROM activities WHERE is_deleted = 0 AND YEAR(date) = :year", nativeQuery = true)
    List<Activity> getActivitiesByYear(@Param("year") String year);

    List<Activity> findAllByIsDeleted(boolean isDeleted);

    Activity save(Activity activity);
}
