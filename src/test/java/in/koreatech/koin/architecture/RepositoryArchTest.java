package in.koreatech.koin.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "in.koreatech.koin", importOptions = ImportOption.DoNotIncludeTests.class)
class RepositoryArchTest {

    @ArchTest
    static final ArchRule repositories_should_not_extend_jpa_repository =
        noClasses().that().areInterfaces()
            .and().resideInAPackage("..repository..")
            .should().beAssignableTo(JpaRepository.class)
            .because("Repository는 JpaRepository 대신 Repository<T, ID>를 사용하고 필요한 메서드만 명시적으로 선언해야 합니다");

    @ArchTest
    static final ArchRule jpa_query_factory_should_only_be_used_in_repository_impl =
        noClasses().that()
            .haveSimpleNameNotEndingWith("RepositoryImpl")
            .and().haveSimpleNameNotEndingWith("QueryRepository")
            .and().haveSimpleNameNotEndingWith("CustomRepository")
            .and().doNotHaveSimpleName("QueryDslConfig")
            .should().dependOnClassesThat()
            .haveFullyQualifiedName("com.querydsl.jpa.impl.JPAQueryFactory")
            .because("JPAQueryFactory는 *RepositoryImpl, *QueryRepository, QueryDslConfig에서만 사용해야 합니다");
}
