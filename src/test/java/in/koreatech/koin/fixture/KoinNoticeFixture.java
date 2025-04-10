package in.koreatech.koin.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.admin.notice.model.KoinNotice;
import in.koreatech.koin.admin.notice.repository.AdminKoinNoticeRepository;
import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.Board;

@Component
public class KoinNoticeFixture {

    private final AdminKoinNoticeRepository adminKoinNoticeRepository;

    public KoinNoticeFixture(AdminKoinNoticeRepository adminKoinNoticeRepository) {
        this.adminKoinNoticeRepository = adminKoinNoticeRepository;
    }

    public Article 코인_캠퍼스_공지사항(Admin admin, Board board) {
        KoinNotice koinNotice = KoinNotice.builder()
            .admin(admin)
            .isDeleted(false)
            .build();

        Article article = Article.builder()
            .board(board)
            .title("[코인 캠퍼스팀] 공지사항 테스트")
            .content("<p>내용</p>")
            .koinNotice(koinNotice)
            .isNotice(true)
            .isDeleted(false)
            .build();

        koinNotice.setArticle(article);
        return adminKoinNoticeRepository.save(article);
    }
}
