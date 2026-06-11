package tn.esprit.lostfoundservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.lostfoundservice.handler.GlobalExceptionHandler;
import tn.esprit.lostfoundservice.service.ItemReportService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemReportController.class)
@Import(GlobalExceptionHandler.class)
class ItemReportControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemReportService itemReportService;

    @Test
    void createReport_shouldReturnForbidden_whenUserHeaderMissing() throws Exception {
        String body = """
                {
                  "reason": "Spam",
                  "details": "Suspicious content"
                }
                """;

        mockMvc.perform(post("/api/v1/reports/items/1")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void getReportsForModeration_shouldReturnForbidden_whenRoleMissing() throws Exception {
        mockMvc.perform(get("/api/v1/reports"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getReportsForModeration_shouldReturnOk_whenRoleIsAdmin() throws Exception {
        when(itemReportService.getReportsByStatus(null)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/reports")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }
}
