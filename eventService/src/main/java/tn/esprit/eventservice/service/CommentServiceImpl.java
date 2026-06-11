package tn.esprit.eventservice.service;

import org.springframework.stereotype.Service;
import tn.esprit.eventservice.dto.CommentDTO;
import tn.esprit.eventservice.entity.Comment;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.exception.BusinessException;
import tn.esprit.eventservice.exception.ResourceNotFoundException;
import tn.esprit.eventservice.repository.CommentRepository;
import tn.esprit.eventservice.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentServiceImpl implements ICommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;

    public CommentServiceImpl(CommentRepository commentRepository,
                              EventRepository eventRepository) {
        this.commentRepository = commentRepository;
        this.eventRepository = eventRepository;
    }

    private CommentDTO toDTO(Comment c) {
        CommentDTO dto = new CommentDTO();
        dto.setId(c.getId());
        dto.setContent(c.getContent());
        dto.setAuthorName(c.getAuthorName());
        dto.setAuthorEmail(c.getAuthorEmail());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        dto.setEventId(c.getEvent().getId());
        return dto;
    }

    @Override
    public CommentDTO addComment(CommentDTO dto) {
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Événement introuvable : " + dto.getEventId()));

        Comment comment = new Comment();
        comment.setContent(dto.getContent());
        comment.setAuthorName(dto.getAuthorName());
        comment.setAuthorEmail(dto.getAuthorEmail());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setEvent(event);

        return toDTO(commentRepository.save(comment));
    }

    @Override
    public CommentDTO updateComment(Long id, CommentDTO dto) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Commentaire introuvable : " + id));

        if (!comment.getAuthorEmail().equals(dto.getAuthorEmail()))
            throw new BusinessException("Vous ne pouvez modifier que vos propres commentaires");

        comment.setContent(dto.getContent());
        comment.setUpdatedAt(LocalDateTime.now());

        return toDTO(commentRepository.save(comment));
    }

    @Override
    public void deleteComment(Long id) {
        commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Commentaire introuvable : " + id));
        commentRepository.deleteById(id);
    }

    @Override
    public List<CommentDTO> getCommentsByEvent(Long eventId) {
        return commentRepository.findByEventIdOrderByCreatedAtDesc(eventId)
                .stream().map(this::toDTO).toList();
    }

    @Override
    public long countCommentsByEvent(Long eventId) {
        return commentRepository.countByEventId(eventId);
    }
}