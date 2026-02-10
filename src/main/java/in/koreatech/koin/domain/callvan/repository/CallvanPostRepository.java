package in.koreatech.koin.domain.callvan.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.callvan.model.CallvanPost;

public interface CallvanPostRepository extends Repository<CallvanPost, Integer> {

    CallvanPost save(CallvanPost callvanPost);
}
