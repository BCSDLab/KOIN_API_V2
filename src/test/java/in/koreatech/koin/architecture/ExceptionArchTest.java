package in.koreatech.koin.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import in.koreatech.koin.global.exception.custom.KoinException;

@AnalyzeClasses(packages = "in.koreatech.koin", importOptions = ImportOption.DoNotIncludeTests.class)
class ExceptionArchTest {

    @ArchTest
    static final ArchRule domain_exceptions_should_extend_koin_exception =
        classes().that().resideInAPackage("..exception..")
            .and().haveSimpleNameEndingWith("Exception")
            .and().doNotHaveFullyQualifiedName(KoinException.class.getName())
            .and().doNotHaveFullyQualifiedName("in.koreatech.koin.global.exception.CustomException")
            .and().doNotHaveFullyQualifiedName(
                "in.koreatech.koin.domain.payment.gateway.toss.exception.TossPaymentException")
            .should().beAssignableTo(KoinException.class)
            .because("커스텀 예외는 KoinException 계층을 상속해야 합니다");
}
