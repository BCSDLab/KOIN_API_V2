package in.koreatech.koin.domain.bus.service.shuttle;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.bus.enums.ShuttleBusRegion;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteType;
import in.koreatech.koin.domain.bus.exception.BusNotFoundException;
import in.koreatech.koin.domain.bus.service.shuttle.model.Route;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusRoute;
import in.koreatech.koin.domain.bus.service.shuttle.model.ShuttleBusSimpleRoute;

public interface ShuttleBusRepository extends Repository<ShuttleBusRoute, ObjectId> {

    ShuttleBusRoute save(ShuttleBusRoute route);

    List<ShuttleBusRoute> findBySemesterType(String semesterType);

    Optional<ShuttleBusRoute> findById(String id);

    default ShuttleBusRoute getById(String id) {
        return findById(id).orElseThrow(
            () -> BusNotFoundException.withDetail("id: " + id));
    }

    /**
     * 교통편 조회를 위해 DB 데이터를 간소화하여 찾는다.
     */
    @Aggregation(pipeline = {
        // 1단계: 'semester_type' 필드가 입력값과 일치하는 문서만 필터링
        "{ $match: { 'semester_type': ?0 } }",

        // 2단계: 'route_info' 배열을 개별 문서로 분해 (배열 내 요소들을 개별적으로 처리하기 위함)
        "{ $unwind: '$route_info' }",

        // 3단계: 'route_info.running_days' 필드가 입력된 요일과 일치하는 데이터만 필터링
        "{ $match: { 'route_info.running_days': ?1 } }",

        // 4단계: 필요한 필드만 선택하여 반환 (불필요한 필드 제외)
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
