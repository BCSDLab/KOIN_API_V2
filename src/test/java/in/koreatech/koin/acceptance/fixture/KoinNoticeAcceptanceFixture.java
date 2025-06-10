package in.koreatech.koin.acceptance.fixture;

import org.springframework.stereotype.Component;

import in.koreatech.koin.admin.notice.model.KoinNotice;
import in.koreatech.koin.admin.notice.repository.AdminKoinNoticeRepository;
import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.Board;

@Component
@SuppressWarnings("NonAsciiCharacters")
public class KoinNoticeAcceptanceFixture {

    private final AdminKoinNoticeRepository adminKoinNoticeRepository;

    public KoinNoticeAcceptanceFixture(AdminKoinNoticeRepository adminKoinNoticeRepository) {
        this.adminKoinNoticeRepository = adminKoinNoticeRepository;
    }

    public Article 코인_공지_게시글(String title, String content,Admin admin, Board board) {
        KoinNotice koinNotice = KoinNotice.builder()
            .admin(admin)
            .isDeleted(false)
            .build();

        Article adminNoticeArticle = Article.builder()
            .board(board)
            .title(title)
            .content(content)
            .koinNotice(koinNotice)
            .isNotice(true)
            .isDeleted(false)
            .build();

        koinNotice.setArticle(adminNoticeArticle);
        return adminKoinNoticeRepository.save(adminNoticeArticle);
    }
}
