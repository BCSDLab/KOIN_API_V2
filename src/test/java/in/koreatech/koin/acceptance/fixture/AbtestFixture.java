package in.koreatech.koin.acceptance.fixture;

import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.admin.abtest.model.Abtest;
import in.koreatech.koin.admin.abtest.model.AbtestStatus;
import in.koreatech.koin.admin.abtest.model.AbtestVariable;
import in.koreatech.koin.admin.abtest.repository.AbtestRepository;

@Component
public final class AbtestFixture {

    private final AbtestRepository abtestRepository;

    public AbtestFixture(AbtestRepository abtestRepository) {
        this.abtestRepository = abtestRepository;
    }

    public Abtest 식단_UI_실험() {
        Abtest abtest =
            Abtest.builder()
                .title("dining_ui_test")
                .displayTitle("식단_UI_실험")
                .description("세부설명")
                .creator("송선권")
                .team("campus")
                .status(AbtestStatus.IN_PROGRESS)
                .build();

        AbtestVariable abtestVariable =
            AbtestVariable.builder()
                .abtest(abtest)
                .name("A")
                .displayName("실험군 A")
                .rate(50)
                .count(0)
                .build();

        AbtestVariable abtestVariable2 =
            AbtestVariable.builder()
                .abtest(abtest)
                .name("B")
                .displayName("실험군 B")
                .rate(50)
                .count(0)
                .build();

        abtest.getAbtestVariables().addAll(List.of(abtestVariable, abtestVariable2));
        return abtestRepository.save(abtest);
    }

    public Abtest 식단_UI_실험(int titleNumber) {
        Abtest abtest =
            Abtest.builder()
                .title("dining_ui_test" + titleNumber)
                .displayTitle("식단_UI_실험")
                .description("세부설명")
                .creator("송선권")
                .team("campus")
                .status(AbtestStatus.IN_PROGRESS)
                .build();

        AbtestVariable abtestVariable =
            AbtestVariable.builder()
                .abtest(abtest)
                .name("A")
                .displayName("실험군 A")
                .rate(50)
                .count(0)
                .build();

        AbtestVariable abtestVariable2 =
            AbtestVariable.builder()
                .abtest(abtest)
                .name("B")
                .displayName("실험군 B")
                .rate(50)
                .count(0)
                .build();

        abtest.getAbtestVariables().addAll(List.of(abtestVariable, abtestVariable2));
        return abtestRepository.save(abtest);

    }

    public Abtest 주변상점_UI_실험() {
        return abtestRepository.save(
            Abtest.builder()
                .title("shop_ui_test")
                .displayTitle("주변상점_UI_실험")
                .description("세부설명")
                .creator("송선권")
                .team("campus")
                .status(AbtestStatus.IN_PROGRESS)
                .build()
        );
    }
}
