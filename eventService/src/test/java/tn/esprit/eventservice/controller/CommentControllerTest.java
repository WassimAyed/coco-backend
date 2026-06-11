package tn.esprit.eventservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.eventservice.dto.CommentDTO;
import tn.esprit.eventservice.service.ICommentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CommentControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(this.context)
                .build();
    }

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ICommentService commentService;

    @Test
    @DisplayName("addComment — should return 201 when valid")
    void addComment_shouldReturn201() throws Exception {
        CommentDTO dto = CommentDTO.builder().content("Great!").eventId(1L).authorName("John").authorEmail("j@j.com").build();
        CommentDTO created = CommentDTO.builder().id(1L).content("Great!").build();

        given(commentService.addComment(any(CommentDTO.class))).willReturn(created);

        mockMvc.perform(post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("updateComment — should return 200 when updated")
    void updateComment_shouldReturn200() throws Exception {
        CommentDTO dto = CommentDTO.builder().content("Updated!").eventId(1L).authorName("John").authorEmail("j@j.com").build();
        CommentDTO updated = CommentDTO.builder().id(1L).content("Updated!").build();

        given(commentService.updateComment(eq(1L), any(CommentDTO.class))).willReturn(updated);

        mockMvc.perform(put("/api/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated!"));
    }

    @Test
    @DisplayName("deleteComment — should return 204 when deleted")
    void deleteComment_shouldReturn204() throws Exception {
        doNothing().when(commentService).deleteComment(1L);

        mockMvc.perform(delete("/api/comments/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("getByEvent — should return list of comments")
    void getByEvent_shouldReturn200() throws Exception {
        CommentDTO dto = CommentDTO.builder().id(1L).content("Test").build();
        given(commentService.getCommentsByEvent(10L)).willReturn(List.of(dto));

        mockMvc.perform(get("/api/comments/event/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("countByEvent — should return comment count")
    void countByEvent_shouldReturn200() throws Exception {
        given(commentService.countCommentsByEvent(10L)).willReturn(3L);

        mockMvc.perform(get("/api/comments/event/10/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }
}
