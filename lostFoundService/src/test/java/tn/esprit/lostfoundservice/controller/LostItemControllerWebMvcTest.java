package tn.esprit.lostfoundservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.lostfoundservice.DTO.LostItemResponseDTO;
import tn.esprit.lostfoundservice.entity.LostItemStatus;
import tn.esprit.lostfoundservice.entity.LostItemType;
import tn.esprit.lostfoundservice.handler.GlobalExceptionHandler;
import tn.esprit.lostfoundservice.service.AiSimilarityClient;
import tn.esprit.lostfoundservice.service.LostItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LostItemController.class)
@Import(GlobalExceptionHandler.class)
class LostItemControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LostItemService lostItemService;

    @MockBean
    private AiSimilarityClient aiSimilarityClient;

    @Test
    void createItem_shouldReturnForbidden_whenUserHeaderMissing() throws Exception {
        String body = """
                {
                  "title": "Wallet",
                  "description": "Black wallet",
                  "type": "LOST",
                  "category": "Accessories",
                  "location": "Campus",
                  "contactInfo": "mail@test.com"
                }
                """;

        mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllItems_shouldReturnOk_withPagedResponse() throws Exception {
        LostItemResponseDTO dto = LostItemResponseDTO.builder()
                .id(1L)
                .title("Wallet")
                .description("Black wallet")
                .type(LostItemType.LOST)
                .status(LostItemStatus.ACTIVE)
                .build();

        when(lostItemService.getAllItems(any(), eq(null))).thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/v1/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Wallet"));
    }

        @Test
        void uploadImage_shouldReturnInternalServerError_whenContentTypeNotAllowed() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "note.txt",
                "text/plain",
                "invalid".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/items/images/upload").file(invalidFile))
                .andExpect(status().isInternalServerError());
    }
}
