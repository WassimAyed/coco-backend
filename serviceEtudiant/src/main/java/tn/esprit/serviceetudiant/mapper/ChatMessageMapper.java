package tn.esprit.serviceetudiant.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import tn.esprit.serviceetudiant.dto.ChatMessageResponse;
import tn.esprit.serviceetudiant.entity.ChatMessage;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ChatMessageMapper {

    ChatMessageResponse toResponse(ChatMessage entity);

    List<ChatMessageResponse> toResponseList(List<ChatMessage> entities);
}