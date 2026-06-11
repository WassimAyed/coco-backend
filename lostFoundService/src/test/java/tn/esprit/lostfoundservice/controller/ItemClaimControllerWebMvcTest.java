package tn.esprit.lostfoundservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.lostfoundservice.handler.GlobalExceptionHandler;
import tn.esprit.lostfoundservice.service.ItemClaimService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemClaimController.class)
@Import(GlobalExceptionHandler.class)
class ItemClaimControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemClaimService itemClaimService;

    @Test
    void createClaim_shouldReturnForbidden_whenUserHeaderMissing() throws Exception {
        String body = """
                {
                  "proofMessage": "I can describe the object"
                }
                """;

        mockMvc.perform(post("/api/v1/claims/items/1")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyClaims_shouldReturnOk_whenUserHeaderPresent() throws Exception {
        when(itemClaimService.getMyClaims(10L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/claims/my")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk());
    }
}
