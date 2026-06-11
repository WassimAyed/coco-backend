package tn.esprit.eventservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.eventservice.dto.CommentDTO;
import tn.esprit.eventservice.entity.Comment;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.exception.BusinessException;
import tn.esprit.eventservice.exception.ResourceNotFoundException;
import tn.esprit.eventservice.repository.CommentRepository;
import tn.esprit.eventservice.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock CommentRepository commentRepository;
    @Mock EventRepository eventRepository;
    @InjectMocks CommentServiceImpl commentService;

    private Event event;
    private Comment comment;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setId(1L);
        event.setName("Tech Summit");

        comment = new Comment();
        comment.setId(1L);
        comment.setContent("Super événement!");
        comment.setAuthorName("Ali");
        comment.setAuthorEmail("ali@test.com");
        comment.setCreatedAt(LocalDateTime.now());
        comment.setEvent(event);
    }

    @Test
    void shouldAddComment() {
        CommentDTO dto = new CommentDTO();
        dto.setEventId(1L);
        dto.setContent("Super événement!");
        dto.setAuthorName("Ali");
        dto.setAuthorEmail("ali@test.com");

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(commentRepository.save(any())).thenReturn(comment);

        CommentDTO result = commentService.addComment(dto);

        assertNotNull(result);
        assertEquals("Super événement!", result.getContent());
        verify(commentRepository).save(any());
    }

    @Test
    void shouldThrowWhenAddingCommentToNonExistentEvent() {
        CommentDTO dto = new CommentDTO();
        dto.setEventId(99L);

        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.addComment(dto));
    }

    @Test
    void shouldUpdateComment() {
        CommentDTO dto = new CommentDTO();
        dto.setContent("Mis à jour");
        dto.setAuthorEmail("ali@test.com");

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any())).thenReturn(comment);

        CommentDTO result = commentService.updateComment(1L, dto);

        assertNotNull(result);
        verify(commentRepository).save(any());
    }

    @Test
    void shouldThrowWhenUpdatingCommentWithWrongEmail() {
        CommentDTO dto = new CommentDTO();
        dto.setContent("Mis à jour");
        dto.setAuthorEmail("autre@test.com"); // mauvais email

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThrows(BusinessException.class, () -> commentService.updateComment(1L, dto));
    }

    @Test
    void shouldDeleteComment() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(1L);

        verify(commentRepository).deleteById(1L);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentComment() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.deleteComment(99L));
    }

    @Test
    void shouldGetCommentsByEvent() {
        when(commentRepository.findByEventIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(comment));

        List<CommentDTO> result = commentService.getCommentsByEvent(1L);

        assertEquals(1, result.size());
        assertEquals("Ali", result.get(0).getAuthorName());
    }

    @Test
    void shouldCountCommentsByEvent() {
        when(commentRepository.countByEventId(1L)).thenReturn(5L);

        long count = commentService.countCommentsByEvent(1L);

        assertEquals(5L, count);
    }
}