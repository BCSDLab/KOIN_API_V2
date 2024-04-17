package in.koreatech.koin.fixture;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.activity.model.Activity;
import in.koreatech.koin.domain.activity.repository.ActivityRepository;

@Component
public final class ActivityFixture {

    private final ActivityRepository activityRepository;

    @Autowired
    public ActivityFixture(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public ActivityBuilder builder() {
        return new ActivityBuilder();
    }

    public final class ActivityBuilder {
        private String title;
        private String description;
        private String imageUrls;
        private LocalDate date;
        private boolean isDeleted;

        public ActivityBuilder title(String title) {
            this.title = title;
            return this;
        }

        public ActivityBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ActivityBuilder imageUrls(String imageUrls) {
            this.imageUrls = imageUrls;
            return this;
        }

        public ActivityBuilder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public ActivityBuilder isDeleted(boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        public Activity build() {
            return activityRepository.save(
                Activity.builder()
                    .title(title)
                    .description(description)
                    .imageUrls(imageUrls)
                    .date(date)
                    .isDeleted(isDeleted)
                    .build()
            );
        }
    }
}
