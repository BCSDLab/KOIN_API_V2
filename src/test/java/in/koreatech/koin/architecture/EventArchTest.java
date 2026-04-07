package in.koreatech.koin.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "in.koreatech.koin", importOptions = ImportOption.DoNotIncludeTests.class)
class EventArchTest {

    @ArchTest
    static final ArchRule event_classes_should_reside_in_event_package =
        classes().that().haveSimpleNameEndingWith("Event")
            .and().areNotInterfaces()
            .and().haveSimpleNameNotEndingWith("EventListener")
            .and().resideOutsideOfPackage("..model..")
            .should().resideInAPackage("..event..")
            .because("Event 클래스는 event 패키지에 위치해야 합니다");

    @ArchTest
    static final ArchRule event_listeners_should_have_proper_name =
        classes().that().haveSimpleNameEndingWith("EventListener")
            .should().resideInAnyPackage("..eventlistener..", "..event..", "..service..")
            .because("EventListener 클래스는 eventlistener, event 또는 service 패키지에 위치해야 합니다");
}
