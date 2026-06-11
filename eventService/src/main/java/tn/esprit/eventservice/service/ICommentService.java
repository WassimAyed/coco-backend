package tn.esprit.eventservice.service;

import tn.esprit.eventservice.dto.CommentDTO;
import java.util.List;

public interface ICommentService {
    CommentDTO addComment(CommentDTO dto);
    CommentDTO updateComment(Long id, CommentDTO dto);
    void deleteComment(Long id);
    List<CommentDTO> getCommentsByEvent(Long eventId);
    long countCommentsByEvent(Long eventId);
}