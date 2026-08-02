package in.koreatech.koin.acceptance.domain;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.departmentcontact.enums.DepartmentCategory;
import in.koreatech.koin.domain.departmentcontact.model.DepartmentContact;
import in.koreatech.koin.domain.departmentcontact.model.DepartmentContactDepartment;

class DepartmentContactApiTest extends AcceptanceTest {

    @BeforeEach
    void setUp() {
        clear();

        createDepartment(DepartmentCategory.EMPLOYMENT, "취창업지원팀", 2,
            new Contact("단기현장실습", "041-560-2632", 6),
            new Contact("구직기술 프로그램, 창업교과", "041-560-2605", 5),
            new Contact("창업", "041-560-2606", 4),
            new Contact("취업네트워크, 취업통계, 예산", "041-560-2603", 3),
            new Contact("취업전담교수", "041-560-2601", 2),
            new Contact("취창업지원팀, IPP센터 총괄", "041-560-2600", 1));
        createDepartment(DepartmentCategory.EMPLOYMENT, "IPP 센터", 1,
            new Contact("장기현장실습(IPP)", "041-560-2630", 1));
        createDepartment(DepartmentCategory.STUDENT_SUPPORT, "학생지원팀", 1,
            new Contact("학생지도", "041-560-2531", 2),
            new Contact("학생지원팀 총괄", "041-560-2530", 1));
        createDepartment(DepartmentCategory.ACADEMIC, "학사팀", 17,
            new Contact("학사팀 업무총괄", "041-560-2539", 1));
        createDepartment(DepartmentCategory.ACADEMIC, "Edutech 센터", 1,
            new Contact("", "041-580-4707", 1));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void 전체_연락처를_카테고리와_부서_단위로_조회한다() throws Exception {
        mockMvc.perform(get("/department-contacts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.updated_at", not(blankOrNullString())))
            .andExpect(jsonPath("$.categories", hasSize(6)))
            .andExpect(jsonPath("$.categories[0].category").value("ACADEMIC"))
            .andExpect(jsonPath("$.categories[0].category_name").value("학사 / 수업"))
            .andExpect(jsonPath("$.categories[0].departments", hasSize(2)))
            .andExpect(jsonPath("$.categories[0].departments[0].name").value("학사팀"))
            .andExpect(jsonPath("$.categories[0].departments[1].name").value("Edutech 센터"))
            .andExpect(jsonPath("$.categories[1].category").value("STUDENT_SUPPORT"))
            .andExpect(jsonPath("$.categories[2].category").value("EMPLOYMENT"))
            .andExpect(jsonPath("$.categories[2].departments", hasSize(2)))
            .andExpect(jsonPath("$.categories[2].departments[0].name").value("IPP 센터"))
            .andExpect(jsonPath("$.categories[2].departments[0].is_single_contact").value(true))
            .andExpect(jsonPath("$.categories[2].departments[1].name").value("취창업지원팀"))
            .andExpect(jsonPath("$.categories[2].departments[1].is_single_contact").value(false));

        mockMvc.perform(get("/department-contacts/ACADEMIC"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.departments[0].name").value("학사팀"));
    }

    @Test
    void 카테고리별로_조회하고_카테고리명으로_전체_검색한다() throws Exception {
        mockMvc.perform(get("/department-contacts/EMPLOYMENT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.category").value("EMPLOYMENT"))
            .andExpect(jsonPath("$.category_name").value("취업 / 현장실습"))
            .andExpect(jsonPath("$.departments", hasSize(2)));

        mockMvc.perform(
                get("/department-contacts")
                    .param("keyword", "취업")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.categories", hasSize(1)))
            .andExpect(jsonPath("$.categories[0].category").value("EMPLOYMENT"))
            .andExpect(jsonPath("$.categories[0].departments", hasSize(2)));

        mockMvc.perform(
                get("/department-contacts/EMPLOYMENT")
                    .param("keyword", "취업")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.departments", hasSize(2)));
    }

    @Test
    void 부서명이나_업무가_검색되면_해당_부서의_연락처를_모두_반환한다() throws Exception {
        mockMvc.perform(
                get("/department-contacts/EMPLOYMENT")
                    .param("keyword", " 창업 ")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.departments", hasSize(1)))
            .andExpect(jsonPath("$.departments[0].name").value("취창업지원팀"))
            .andExpect(jsonPath("$.departments[0].is_single_contact").value(false))
            .andExpect(jsonPath("$.departments[0].contacts", hasSize(6)))
            .andExpect(jsonPath("$.departments[0].contacts[0].task").value("취창업지원팀, IPP센터 총괄"))
            .andExpect(jsonPath("$.departments[0].contacts[5].task").value("단기현장실습"));

        mockMvc.perform(
                get("/department-contacts")
                    .param("keyword", "취업통계")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.categories", hasSize(1)))
            .andExpect(jsonPath("$.categories[0].departments", hasSize(1)))
            .andExpect(jsonPath("$.categories[0].departments[0].contacts", hasSize(6)));

        mockMvc.perform(
                get("/department-contacts")
                    .param("keyword", " edutech ")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.categories", hasSize(1)))
            .andExpect(jsonPath("$.categories[0].departments[0].name").value("Edutech 센터"))
            .andExpect(jsonPath("$.categories[0].departments[0].is_single_contact").value(true));

        mockMvc.perform(
                get("/department-contacts")
                    .param("keyword", "IPP센터")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.categories", hasSize(1)))
            .andExpect(jsonPath("$.categories[0].category").value("EMPLOYMENT"))
            .andExpect(jsonPath("$.categories[0].departments[0].name").value("IPP 센터"));
    }

    @Test
    void 검색_결과가_없으면_빈_목록을_반환하고_잘못된_카테고리는_거부한다() throws Exception {
        mockMvc.perform(
                get("/department-contacts")
                    .param("keyword", "041-560-2606")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.categories", empty()));

        mockMvc.perform(
                get("/department-contacts/EMPLOYMENT")
                    .param("keyword", "존재하지 않는 부서")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.category").value("EMPLOYMENT"))
            .andExpect(jsonPath("$.departments", empty()));

        mockMvc.perform(get("/department-contacts/UNKNOWN"))
            .andExpect(status().isBadRequest());
    }

    private void createDepartment(
        DepartmentCategory category,
        String name,
        int displayOrder,
        Contact... contacts
    ) {
        DepartmentContactDepartment department = new DepartmentContactDepartment(category, name, displayOrder);
        entityManager.persist(department);
        for (Contact contact : contacts) {
            entityManager.persist(new DepartmentContact(
                department,
                contact.task(),
                contact.phoneNumber(),
                contact.displayOrder()
            ));
        }
    }

    private record Contact(String task, String phoneNumber, int displayOrder) {
    }
}
