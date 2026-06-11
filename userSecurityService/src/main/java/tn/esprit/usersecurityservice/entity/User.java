package tn.esprit.usersecurityservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

import lombok.*;
import tn.esprit.usersecurityservice.Enum.Role;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements  UserDetails{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Column(unique = true)
    private String username;

    @NotBlank
    private String lastname ;

    @Email
    @NotBlank(message = "Email is required")
    @Column(unique = true)
    private String email;

    private String phone;

    @NotBlank(message = "Password is required")
    private String password;
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @OneToOne(mappedBy="user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private RefreshToken refreshToken;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;   // account active?

    @Column(nullable = false)
    @Builder.Default
    private boolean locked = false; // blocked account

    @Column(nullable = false)
    @Builder.Default
    private boolean twoFactorEnabled = false;

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
