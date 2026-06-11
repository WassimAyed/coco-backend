package tn.esprit.collocationservice.Service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSecurityClient Unit Tests")
class UserSecurityClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UserSecurityClient client;

    // =========================================================================
    // FIND USER BY ID
    // =========================================================================
    @Nested
    @DisplayName("findUserById()")
    class FindUserByIdTests {

        @Test
        @DisplayName("should return UserDTO when REST call succeeds")
        void findUserById_whenSucceeds_shouldReturnUser() {
            // Arrange
            Long ownerId = 100L;
            UserDTO expected = new UserDTO();
            expected.setId(ownerId);
            expected.setEmail("owner@example.com");
            expected.setFirstName("Karim");
            expected.setLastName("Mokaddem");

            String url = "http://localhost:8090/users/" + ownerId;
            when(restTemplate.getForObject(url, UserDTO.class)).thenReturn(expected);

            // Act
            UserDTO result = client.findUserById(ownerId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(ownerId);
            assertThat(result.getEmail()).isEqualTo("owner@example.com");
            assertThat(result.getFirstName()).isEqualTo("Karim");
            verify(restTemplate).getForObject(url, UserDTO.class);
        }

        @Test
        @DisplayName("should return null when REST call returns null")
        void findUserById_whenApiReturnsNull_shouldReturnNull() {
            Long ownerId = 200L;
            String url = "http://localhost:8090/users/" + ownerId;
            when(restTemplate.getForObject(url, UserDTO.class)).thenReturn(null);

            UserDTO result = client.findUserById(ownerId);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should return null and not throw when REST call throws exception")
        void findUserById_whenRestThrows_shouldReturnNull() {
            Long ownerId = 999L;
            String url = "http://localhost:8090/users/" + ownerId;
            when(restTemplate.getForObject(url, UserDTO.class))
                    .thenThrow(new RestClientException("Connection refused"));

            // Act — should not propagate exception
            UserDTO result = client.findUserById(ownerId);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should build URL with correct owner ID")
        void findUserById_shouldUseCorrectUrl() {
            Long ownerId = 42L;
            String expectedUrl = "http://localhost:8090/users/42";
            when(restTemplate.getForObject(expectedUrl, UserDTO.class)).thenReturn(new UserDTO());

            client.findUserById(ownerId);

            verify(restTemplate).getForObject(expectedUrl, UserDTO.class);
        }

        @Test
        @DisplayName("should return null when RuntimeException (not RestClientException) is thrown")
        void findUserById_whenRuntimeExceptionThrown_shouldReturnNull() {
            Long ownerId = 77L;
            String url = "http://localhost:8090/users/" + ownerId;
            when(restTemplate.getForObject(url, UserDTO.class))
                    .thenThrow(new RuntimeException("Unexpected error"));

            UserDTO result = client.findUserById(ownerId);

            assertThat(result).isNull();
        }
    }
}
