package in.koreatech.koin.batch.campus.bus.school.repository;

import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import in.koreatech.koin.batch.campus.bus.school.model.SchoolBus;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SchoolBusRepository {

    private final MongoTemplate mongoTemplate;

    public SchoolBus upsert(SchoolBus schoolBus) {
        Query query = Query.query(
            Criteria
                .where("region").is(schoolBus.getRegion())
                .and("bus_type").is(schoolBus.getBusType())
                .and("direction").is(schoolBus.getDirection())
        );

        FindAndReplaceOptions options = new FindAndReplaceOptions().upsert();

        return mongoTemplate.findAndReplace(query, schoolBus, options);
    }
}
