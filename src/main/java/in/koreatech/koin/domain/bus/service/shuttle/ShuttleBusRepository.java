package in.koreatech.koin.domain.bus.service.shuttle;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.bus.exception.BusNotFoundException;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusSimpleRoute;

public interface ShuttleBusRepository extends Repository<ShuttleBusRoute, ObjectId> {

    List<ShuttleBusRoute> findBySemesterType(String semesterType);

    Optional<ShuttleBusRoute> findById(String id);

    default ShuttleBusRoute getById(String id) {
        return findById(id).orElseThrow(
            () -> BusNotFoundException.withDetail("id: " + id));
    }

    @Aggregation(pipeline = {
        "{ $match: { 'semester_type': ?0 } }",                   // semesterType, region 필터
        "{ $unwind: '$route_info' }",                                         // route_info 배열 분해
        "{ $match: { 'route_info.running_days': ?1 } }",                      // running_days 필터
        """
        { $project: {
            'route_name': 1,
            'node_name': '$node_info.name',
            'arrival_time': '$route_info.arrival_time',
            '_id': 0
        }}"""
    })
    List<ShuttleBusSimpleRoute> findBySemesterType(String semesterType, String dayOfWeek);
}
