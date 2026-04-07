package in.koreatech.koin.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

import org.springframework.beans.factory.annotation.Autowired;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "in.koreatech.koin", importOptions = ImportOption.DoNotIncludeTests.class)
class DependencyInjectionArchTest {

    @ArchTest
    static final ArchRule no_field_injection_with_autowired =
        noFields().should().beAnnotatedWith(Autowired.class)
            .because("@Autowired 필드 주입은 금지합니다. @RequiredArgsConstructor를 통한 생성자 주입을 사용해야 합니다");
}
