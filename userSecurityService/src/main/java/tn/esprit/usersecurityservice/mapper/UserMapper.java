package tn.esprit.usersecurityservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import tn.esprit.usersecurityservice.dto.UserResponse;
import tn.esprit.usersecurityservice.entity.User;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapper {

    // Role enum -> String role: MapStruct's default behaviour calls .name() (matches the
    // existing manual code). All other fields are 1:1 by name (id, username, lastname, email,
    // phone, imageUrl, twoFactorEnabled). `phone` is now consistently populated for both
    // /api/auth/me and /admin/users — previously only the admin endpoint filled it.
    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
}