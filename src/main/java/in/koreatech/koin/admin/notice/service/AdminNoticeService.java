package in.koreatech.koin.admin.notice.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.notice.dto.AdminNoticeRequest;
import in.koreatech.koin.admin.notice.dto.AdminNoticeResponse;
import in.koreatech.koin.admin.notice.dto.AdminNoticesResponse;
import in.koreatech.koin.admin.notice.repository.AdminNoticeRepository;
import in.koreatech.koin.global.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminNoticeService {

    private final AdminNoticeRepository adminNoticeRepository;

    private static final Sort NOTICES_SORT = Sort.by(
        Sort.Order.desc("id")
    );

    @Transactional
    public void createNotice(AdminNoticeRequest request) {
        var notice = request.toNotice();
        adminNoticeRepository.save(notice);
    }

    public AdminNoticesResponse getNotices(Integer page, Integer limit, Boolean isDeleted) {
        Long total = adminNoticeRepository.countAllByIsDeleted(isDeleted);
        Criteria criteria = Criteria.of(page, limit, total.intValue());
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), NOTICES_SORT);
        var result = adminNoticeRepository.findAllByIsDeleted(isDeleted, pageRequest);

        return AdminNoticesResponse.of(result, criteria);
    }

    public AdminNoticeResponse getNotice(Integer noticeId) {
        var notice = adminNoticeRepository.getById(noticeId);
        return AdminNoticeResponse.from(notice);
    }

    @Transactional
    public void deleteNotice(Integer noticeId) {
        var notice = adminNoticeRepository.getById(noticeId);
        notice.delete();
    }

    @Transactional
    public void updateNotice(Integer noticeId, AdminNoticeRequest request) {
        var notice = adminNoticeRepository.getById(noticeId);
        notice.update(
            /*--*/
        );
    }
}
