package in.koreatech.koin.domain.bus.service.shuttle;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import in.koreatech.koin.domain.bus.exception.BusNotFoundException;
import in.koreatech.koin.domain.bus.service.shuttle.model.Route;
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

    @Aggregation(pipeline = {
        "{ $match: { 'semester_type': ?0, 'route_type': ?1 } }",
        "{ $unwind: '$route_info' }",
        """
            { $project: {
                '_id': 0,
                'route_name': 1,
                'region': 1,
                'route_type': 1,
                'route_info': '$route_info.name',
                'route_detail': '$route_info.detail',
                'running_days': '$route_info.running_days',
                'arrival_nodes': {
                    $map: {
                        input: { $range: [0, { $size: '$node_info' }] },
                        as: 'index',
                        in: {
                            'node_name': { '$arrayElemAt': ['$node_info.name', '$$index'] },
                            'arrival_time': { '$arrayElemAt': ['$route_info.arrival_time', '$$index'] }
                        }
                    }
                }
            }}"""
    })
    List<Route> findAllBySemesterTypeAndRouteType(String semesterType, ShuttleRouteType routeType);
}
