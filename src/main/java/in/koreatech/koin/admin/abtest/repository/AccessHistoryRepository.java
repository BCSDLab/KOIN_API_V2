package in.koreatech.koin.admin.abtest.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.admin.abtest.exception.AccessHistoryNotFoundException;
import in.koreatech.koin.admin.abtest.model.AccessHistory;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface AccessHistoryRepository extends Repository<AccessHistory, Integer> {

    AccessHistory save(AccessHistory accessHistory);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("SELECT a FROM AccessHistory a JOIN FETCH a.device WHERE a.id = :id")
    Optional<AccessHistory> findById(Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("SELECT a FROM AccessHistory a JOIN FETCH a.device WHERE a.device.id = :deviceId")
    Optional<AccessHistory> findByDeviceId(@Param("deviceId") Integer deviceId);

    default AccessHistory getById(Integer accessHistoryId) {
        return findById(accessHistoryId).orElseThrow(() ->
            AccessHistoryNotFoundException.withDetail("accessHistoryId: " + accessHistoryId));
    }

    default AccessHistory getByDeviceId(Integer deviceId) {
        return findByDeviceId(deviceId).orElseThrow(() ->
            AccessHistoryNotFoundException.withDetail("deviceId: " + deviceId));
    }
}
