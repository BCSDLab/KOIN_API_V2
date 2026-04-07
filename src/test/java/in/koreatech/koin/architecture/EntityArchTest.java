package in.koreatech.koin.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.Entity;

@AnalyzeClasses(packages = "in.koreatech.koin", importOptions = ImportOption.DoNotIncludeTests.class)
class EntityArchTest {

    @ArchTest
    static final ArchRule entities_should_not_use_setter =
        noClasses().that().areAnnotatedWith(Entity.class)
            .should(haveAnnotationWithName("Setter"))
            .because("@Entity 클래스에 @Setter를 사용하면 안 됩니다. 상태 변경은 비즈니스 메서드를 통해 수행해야 합니다");

    @ArchTest
    static final ArchRule entities_should_not_use_data =
        noClasses().that().areAnnotatedWith(Entity.class)
            .should(haveAnnotationWithName("Data"))
            .because("@Entity 클래스에 @Data를 사용하면 안 됩니다. @Data는 @Setter를 포함합니다");

    private static ArchCondition<JavaClass> haveAnnotationWithName(String annotationSimpleName) {
        return new ArchCondition<>("have annotation @" + annotationSimpleName) {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasAnnotation = javaClass.getAnnotations().stream()
                    .map(JavaAnnotation::getRawType)
                    .map(JavaClass::getSimpleName)
                    .anyMatch(name -> name.equals(annotationSimpleName));
                if (!hasAnnotation) {
                    events.add(SimpleConditionEvent.violated(
                        javaClass,
                        String.format("%s에 @%s 어노테이션이 없습니다", javaClass.getName(), annotationSimpleName)
                    ));
                }
            }
        };
    }
}
