package in.koreatech.koin.domain.coop.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.coop.model.ExcelDownloadCache;
import in.koreatech.koin.global.config.repository.RedisRepository;

@RedisRepository
public interface ExcelDownloadCacheRepository extends Repository<ExcelDownloadCache, String> {

    ExcelDownloadCache save(ExcelDownloadCache excelDownloadCache);

    Optional<ExcelDownloadCache> findById(String id);

    boolean existsById(String id);
}
