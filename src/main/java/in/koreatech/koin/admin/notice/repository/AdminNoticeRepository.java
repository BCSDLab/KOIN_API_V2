package in.koreatech.koin.admin.notice.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface AdminNoticeRepository {

    void save(Object notice);

    Long countAllByIsDeleted(Boolean isDeleted);

    Page<?> findAllByIsDeleted(Boolean isDeleted, PageRequest pageRequest);

    Optional<?> findById(Integer noticeId);

    default Object getById(Integer noticeId) {
        return findById(noticeId)
            .orElseThrow();
    }
}
