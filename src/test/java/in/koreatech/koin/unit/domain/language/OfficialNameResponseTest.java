package in.koreatech.koin.unit.domain.language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.language.service.OfficialNameResolver;
import in.koreatech.koin.domain.order.address.controller.AddressController;
import in.koreatech.koin.domain.order.address.dto.CampusDeliveryAddressRequestFilter;
import in.koreatech.koin.domain.order.address.dto.CampusDeliveryAddressResponse;
import in.koreatech.koin.domain.order.address.dto.CampusDeliveryAddressResponse.InnerCampusDeliveryAddressResponse;
import in.koreatech.koin.domain.order.address.service.AddressSearchOpenApiService;
import in.koreatech.koin.domain.order.address.service.CampusAddressSearchService;

class OfficialNameResponseTest {

    @Test
    void 교내주소_표시명만_공식명으로_반환하고_언어별_조회가_원문캐시를_변경하지_않는다() throws Exception {
        var campus = mock(CampusAddressSearchService.class);
        var original = new CampusDeliveryAddressResponse(1, List.of(new InnerCampusDeliveryAddressResponse(
            1, "기숙사", "충남 천안시 생활관 101동", "101동(해울)", "충남 천안시",
            BigDecimal.ONE, BigDecimal.TEN, "101동(해울)")));
        when(campus.getCampusDeliveryAddresses(CampusDeliveryAddressRequestFilter.ALL)).thenReturn(original);
        var mvc = MockMvcBuilders.standaloneSetup(new AddressController(
            mock(AddressSearchOpenApiService.class), campus)).build();

        mvc.perform(get("/address/delivery/campus").param("language", "en-US"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.addresses[0].display_name").value("Haeul"))
            .andExpect(jsonPath("$.addresses[0].full_address").value("충남 천안시 생활관 101동"));
        mvc.perform(get("/address/delivery/campus"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.addresses[0].display_name").value("101동(해울)"));
        assertThat(original.addresses().get(0).displayName()).isEqualTo("101동(해울)");
        assertThat(OfficialNameResolver.buildingName("공학 1관", SupportedLanguage.ENGLISH))
            .isEqualTo("Engineering Building 1");
        assertThat(OfficialNameResolver.buildingName("공학 1관 101호", SupportedLanguage.ENGLISH))
            .isEqualTo("공학 1관 101호");
    }

}
