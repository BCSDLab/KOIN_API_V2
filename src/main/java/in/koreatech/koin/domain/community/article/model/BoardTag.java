package in.koreatech.koin.domain.community.article.model;

import lombok.Getter;

@Getter
public enum BoardTag {
    자유게시판("FA001"),
    취업게시판("JA001"),
    익명게시판("AA001"),
    공지사항("NA000"),
    일반공지("NA001"),
    장학공지("NA002"),
    학사공지("NA003"),
    취업공지("NA004"),
    코인공지("NA005"),
    질문게시판("QA001"),
    홍보게시판("EA001"),
    ;

    private final String tag;

    BoardTag(String tag) {
        this.tag = tag;
    }
}
