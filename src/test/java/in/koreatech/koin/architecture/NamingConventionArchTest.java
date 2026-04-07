package in.koreatech.koin.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import java.util.regex.Pattern;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

@AnalyzeClasses(packages = "in.koreatech.koin", importOptions = ImportOption.DoNotIncludeTests.class)
class NamingConventionArchTest {

    @ArchTest
    static final ArchRule classes_in_controller_package_should_have_proper_name =
        classes().that().resideInAPackage("..controller")
            .and().areTopLevelClasses()
            .should(haveSimpleNameMatchingAny(
                ".*Controller(V\\d+)?",
                ".*Api(V\\d+)?",
                ".*Converter"
            ))
            .because("controller 패키지의 클래스는 Controller, Api 또는 Converter로 끝나야 합니다");

    @ArchTest
    static final ArchRule classes_in_repository_package_should_have_proper_name =
        classes().that().resideInAPackage("..repository")
            .and().areTopLevelClasses()
            .should(haveSimpleNameMatchingAny(
                ".*Repository(V\\d+)?",
                ".*RepositoryImpl"
            ))
            .because("repository 패키지의 클래스는 Repository 또는 RepositoryImpl로 끝나야 합니다");

    @ArchTest
    static final ArchRule classes_in_exception_package_should_have_proper_name =
        classes().that().resideInAPackage("..exception")
            .and().areTopLevelClasses()
            .and().haveSimpleNameNotEndingWith("ErrorCode")
            .and().haveSimpleNameNotEndingWith("ErrorResponse")
            .and().haveSimpleNameNotEndingWith("Response")
            .and().haveSimpleNameNotContaining("package-info")
            .should(haveSimpleNameMatchingAny(".*Exception", ".*ExceptionHandler"))
            .because("exception 패키지의 클래스는 Exception 또는 ExceptionHandler로 끝나야 합니다");

    private static ArchCondition<JavaClass> haveSimpleNameMatchingAny(String... patterns) {
        String description = "have simple name matching any of [" + String.join(", ", patterns) + "]";
        return new ArchCondition<>(description) {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                String simpleName = javaClass.getSimpleName();
                boolean matches = false;
                for (String pattern : patterns) {
                    if (Pattern.matches(pattern, simpleName)) {
                        matches = true;
                        break;
                    }
                }
                if (!matches) {
                    events.add(SimpleConditionEvent.violated(
                        javaClass,
                        String.format("%s의 이름이 허용된 패턴과 일치하지 않습니다", javaClass.getName())
                    ));
                }
            }
        };
    }
}
