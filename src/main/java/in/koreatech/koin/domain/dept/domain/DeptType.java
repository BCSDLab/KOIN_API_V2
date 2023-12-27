package in.koreatech.koin.domain.dept.domain;

import lombok.Getter;

@Getter
public enum DeptType {
    ARCHITECTURAL_ENGINEERING("건축공학부"),
    EMPLOYMENT_SERVICE_POLICY_DEPARTMENT("고용서비스정책학과"),
    MECHANICAL_ENGINEERING("기계공학부"),
    DESIGN_ENGINEERING("디자인공학부"),
    MECHATRONICS_ENGINEERING("메카트로닉스공학부"),
    INDUSTRIAL_MANAGEMENT("산업경영학부"),
    NEW_ENERGY_MATERIALS_CHEMICAL_ENGINEERING("에너지신소재화학공학부"),
    ELECTRICAL_AND_ELECTRONIC_COMMUNICATION_ENGINEERING("전기전자통신공학부"),
    COMPUTER_SCIENCE("컴퓨터공학부");

    private final String deptName;

    DeptType(String deptName) {
        this.deptName = deptName;
    }
}
