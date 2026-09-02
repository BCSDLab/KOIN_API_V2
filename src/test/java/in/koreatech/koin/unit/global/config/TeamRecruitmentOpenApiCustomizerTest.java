package in.koreatech.koin.unit.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.global.config.TeamRecruitmentOpenApiCustomizer;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

class TeamRecruitmentOpenApiCustomizerTest {

    @Test
    void nullablePropertyIsInlinedWithoutMutatingItsSharedComponent() {
        ObjectSchema component = new ObjectSchema();
        component.addProperty("id", new IntegerSchema());
        component.setRequired(List.of("id"));
        component.setNullable(true);
        ObjectSchema response = new ObjectSchema();
        response.addProperty("role", new Schema<>().$ref("#/components/schemas/ApplicationRole"));
        Components components = new Components()
            .addSchemas("ApplicationRole", component)
            .addSchemas("ApplicationCreatedResponse", response);

        new TeamRecruitmentOpenApiCustomizer().customise(new OpenAPI().components(components));

        Schema<?> inline = (Schema<?>)response.getProperties().get("role");
        assertThat(inline).isNotSameAs(component);
        assertThat(inline.getType()).isEqualTo("object");
        assertThat(inline.getProperties()).containsKey("id").isNotSameAs(component.getProperties());
        assertThat(inline.getRequired()).containsExactly("id").isNotSameAs(component.getRequired());
        assertThat(inline.getNullable()).isTrue();
        assertThat(component.getNullable()).isTrue();
    }
}
