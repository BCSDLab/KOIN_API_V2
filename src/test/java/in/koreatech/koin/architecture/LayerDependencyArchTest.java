package in.koreatech.koin.architecture;

import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "in.koreatech.koin", importOptions = ImportOption.DoNotIncludeTests.class)
class LayerDependencyArchTest {

    @ArchTest
    static final ArchRule controller_should_not_depend_on_repository =
        noClasses().that().resideInAPackage("..controller..")
            .should().dependOnClassesThat().resideInAPackage("..repository..")
            .because("Controller는 Repository에 직접 의존하지 않고 Service를 통해 접근해야 합니다");

    @ArchTest
    static final ArchRule service_should_not_depend_on_controller =
        noClasses().that().resideInAPackage("..service..")
            .should().dependOnClassesThat().resideInAPackage("..controller..")
            .because("Service는 Controller에 의존하지 않아야 합니다");

    @ArchTest
    static final ArchRule repository_should_not_depend_on_controller =
        noClasses().that().resideInAPackage("..repository..")
            .should().dependOnClassesThat().resideInAPackage("..controller..")
            .because("Repository는 Controller에 의존하지 않아야 합니다");

    @ArchTest
    static final ArchRule repository_should_not_depend_on_service =
        noClasses().that().resideInAPackage("..repository..")
            .should().dependOnClassesThat(
                resideInAPackage("..service..")
                    .and(describe("bus 도메인 제외",
                        clazz -> !clazz.getPackageName().contains(".bus.service.")))
            )
            .because("Repository는 Service에 의존하지 않아야 합니다 (bus 도메인의 service 하위 model 패키지는 예외)");
}
