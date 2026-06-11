package tn.esprit.serviceetudiant.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import tn.esprit.serviceetudiant.dto.ServiceRequestResponse;
import tn.esprit.serviceetudiant.entity.ServiceRequest;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ServiceRequestMapper {

    ServiceRequestResponse toResponse(ServiceRequest entity);

    List<ServiceRequestResponse> toResponseList(List<ServiceRequest> entities);
}