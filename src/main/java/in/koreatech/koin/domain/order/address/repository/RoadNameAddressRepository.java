package in.koreatech.koin.domain.order.address.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import in.koreatech.koin.domain.order.address.model.RoadNameAddressDocument;

@in.koreatech.koin.config.repository.MongoRepository
public interface RoadNameAddressRepository extends MongoRepository<RoadNameAddressDocument, String> {

    @Query("{ $or: [ " +
        "    { 'road_address' : { $regex: ?0, $options: 'i' } }, " +
        "    { 'jibun_address': { $regex: ?0, $options: 'i' } }, " +
        "    { 'bd_nm'        : { $regex: ?0, $options: 'i' } }  " +
        "] }")
    Page<RoadNameAddressDocument> findByKeyword(String keyword, Pageable pageable);
}
