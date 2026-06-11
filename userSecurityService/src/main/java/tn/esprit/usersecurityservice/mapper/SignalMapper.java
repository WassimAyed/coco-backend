package tn.esprit.usersecurityservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import tn.esprit.usersecurityservice.dto.SignalResponse;
import tn.esprit.usersecurityservice.entity.Signal;

import java.time.Instant;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SignalMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToString")
    SignalResponse toResponse(Signal signal);

    List<SignalResponse> toResponseList(List<Signal> signals);

    // Preserves the null-safe `signal.getCreatedAt() != null ? toString() : null` semantics
    // of the original manual mapping.
    @Named("instantToString")
    default String instantToString(Instant value) {
        return value == null ? null : value.toString();
    }
}