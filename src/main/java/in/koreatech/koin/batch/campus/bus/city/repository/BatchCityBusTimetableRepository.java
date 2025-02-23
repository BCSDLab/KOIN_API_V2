package in.koreatech.koin.batch.campus.bus.city.repository;

import java.util.List;

import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.bulk.BulkWriteResult;

import in.koreatech.koin.batch.campus.bus.city.model.TimetableDocument;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BatchCityBusTimetableRepository {

    private final MongoTemplate mongoTemplate;

    public void saveAll(List<TimetableDocument> timetables) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(
            BulkOperations.BulkMode.UNORDERED,
            TimetableDocument.class
        );

        timetables.forEach(timetable -> {
            Query query = Query.query(Criteria.where("_id").is(timetable.getRouteId()));

            Update update = new Update()
                .set("updatedAt", timetable.getUpdatedAt())
                .set("busInfo", timetable.getBusInfo())
                .set("busTimetables", timetable.getBusTimetables());

            bulkOps.upsert(query, update);
        });

        BulkWriteResult result = bulkOps.execute();

        // log.info("Bulk upsert completed - Upserted: {}, Modified: {}, Matched: {}",
        //     result.getUpserts().size(),
        //     result.getModifiedCount(),
        //     result.getMatchedCount()
        // );
    }
}
