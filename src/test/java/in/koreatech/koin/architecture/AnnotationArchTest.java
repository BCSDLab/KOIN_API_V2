package in.koreatech.koin.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import jakarta.persistence.Entity;

@AnalyzeClasses(packages = "in.koreatech.koin", importOptions = ImportOption.DoNotIncludeTests.class)
class AnnotationArchTest {

    @ArchTest
    static final ArchRule rest_controllers_should_be_in_controller_package =
        classes().that().areAnnotatedWith(RestController.class)
            .and().doNotHaveFullyQualifiedName("in.koreatech.koin.domain.test.TestController")
            .should().resideInAPackage("..controller..")
            .because("@RestController 클래스는 controller 패키지에 위치해야 합니다");

    @ArchTest
    static final ArchRule services_should_be_in_service_or_usecase_package =
        classes().that().areAnnotatedWith(Service.class)
            .should().resideInAnyPackage(
                "..service..",
                "..usecase..",
                "..cache..",
                "..gateway..",
                "..util..",
                "..datacleaner..",
                "..client.."
            )
            .because("@Service 클래스는 service, usecase, cache, gateway 등의 패키지에 위치해야 합니다");

    @ArchTest
    static final ArchRule entities_should_be_in_model_package =
        classes().that().areAnnotatedWith(Entity.class)
            .should().resideInAPackage("..model..")
            .because("@Entity 클래스는 model 패키지에 위치해야 합니다");

    @ArchTest
    static final ArchRule configuration_should_be_in_config_package =
        classes().that().areAnnotatedWith(Configuration.class)
            .should().resideInAnyPackage("..config..")
            .because("@Configuration 클래스는 config 패키지에 위치해야 합니다");
}
