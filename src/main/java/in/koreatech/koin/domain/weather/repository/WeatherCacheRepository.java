package in.koreatech.koin.domain.weather.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.weather.model.WeatherCache;

public interface WeatherCacheRepository extends Repository<WeatherCache, String> {

    WeatherCache save(WeatherCache weatherCache);

    Optional<WeatherCache> findById(String id);
}
