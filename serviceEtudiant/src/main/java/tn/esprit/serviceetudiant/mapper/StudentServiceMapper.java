package tn.esprit.serviceetudiant.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import tn.esprit.serviceetudiant.dto.StudentServiceResponse;
import tn.esprit.serviceetudiant.entity.StudentService;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface StudentServiceMapper {

    @Mapping(target = "tags", source = "tags", qualifiedByName = "copyTags")
    StudentServiceResponse toResponse(StudentService entity);

    List<StudentServiceResponse> toResponseList(List<StudentService> entities);

    // Preserves the immutable-copy semantics of the original `List.copyOf(service.getTags())`
    // call. Returns an empty list for null input so callers never see a NullPointerException.
    @Named("copyTags")
    default List<String> copyTags(List<String> tags) {
        return tags == null ? List.of() : List.copyOf(tags);
    }
}