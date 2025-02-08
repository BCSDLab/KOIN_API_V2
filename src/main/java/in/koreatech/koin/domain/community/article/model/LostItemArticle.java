package in.koreatech.koin.domain.community.article.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.ColumnDefault;

import in.koreatech.koin.domain.shop.model.review.ReportStatus;
import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "lost_item_articles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LostItemArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @NotNull
    @Column(name = "category", nullable = false)
    private String category;

    @Size(max = 255)
    @NotNull
    @Column(name = "found_place", nullable = false)
    private String foundPlace;

    @NotNull
    @Column(name = "found_date", nullable = false)
    private LocalDate foundDate;

    @OneToMany(mappedBy = "lostItemArticle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LostItemImage> images = new ArrayList<>();

    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "lostItemArticle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LostItemReport> lostItemReports = new ArrayList<>();

    @Builder
    public LostItemArticle(
        Article article,
        User author,
        String category,
        String foundPlace,
        LocalDate foundDate,
        List<LostItemImage> images,
        Boolean isDeleted
    ) {
        this.article = article;
        this.author = author;
        this.category = category;
        this.foundPlace = foundPlace;
        this.foundDate = foundDate;
        this.images = images;
        this.isDeleted = isDeleted;
    }

    public void addImage(List<LostItemImage> images) {
        this.images.addAll(images);
    }

    public void removeImage(List<LostItemImage> images) {
        images.remove(images);
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public String generateTitle() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yy.MM.dd");
        return String.format(
            "%s | %s | %s",
            this.category,
            this.foundPlace,
            dateFormatter.format(this.foundDate)
        );
    }

    public void delete() {
        this.isDeleted = true;
        this.images.forEach(LostItemImage::delete);
    }

    /**
     * 미처리된 신고가 존재하는지 확인합니다.
     */
    public boolean isReported() {
        return this.getLostItemReports()
            .stream()
            .anyMatch(report -> report.getReportStatus() == ReportStatus.UNHANDLED);
    }

    /**
     * 특정 사용자에 의해 미처리된 신고가 존재하는지 확인합니다.
     */
    public boolean isReportedByUserId(Integer userId) {
        return this.getLostItemReports()
            .stream()
            .filter(report -> Objects.equals(report.getStudent().getId(), userId))
            .anyMatch(report -> report.getReportStatus() == ReportStatus.UNHANDLED);
    }
}