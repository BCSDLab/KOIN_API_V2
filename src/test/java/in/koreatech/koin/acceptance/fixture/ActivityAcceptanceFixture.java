package in.koreatech.koin.acceptance.fixture;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.activity.model.Activity;
import in.koreatech.koin.domain.activity.repository.ActivityRepository;

@Component
public final class ActivityAcceptanceFixture {

    private final ActivityRepository activityRepository;

    @Autowired
    public ActivityAcceptanceFixture(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public ActivityFixtureBuilder builder() {
        return new ActivityFixtureBuilder();
    }

    public final class ActivityFixtureBuilder {

        private String title;
        private String description;
        private String imageUrls;
        private LocalDate date;
        private boolean isDeleted;

        public ActivityFixtureBuilder title(String title) {
            this.title = title;
            return this;
        }

        public ActivityFixtureBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ActivityFixtureBuilder imageUrls(String imageUrls) {
            this.imageUrls = imageUrls;
            return this;
        }

        public ActivityFixtureBuilder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public ActivityFixtureBuilder isDeleted(boolean isDeleted) {
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
