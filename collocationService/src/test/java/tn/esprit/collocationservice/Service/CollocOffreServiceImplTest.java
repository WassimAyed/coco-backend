package tn.esprit.collocationservice.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.collocationservice.Entity.collocOffre;
import tn.esprit.collocationservice.Entity.collocOffreFilter;
import tn.esprit.collocationservice.Entity.collocOffreImage;
import tn.esprit.collocationservice.Repository.collocOffreImageRepo;
import tn.esprit.collocationservice.Repository.collocOffreRepo;
import tn.esprit.collocationservice.Exception.OfferNotFoundException;
import tn.esprit.collocationservice.Exception.ImageStorageException;

import java.io.IOException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("collocOffreServiceImpl Unit Tests")
class CollocOffreServiceImplTest {

    @Mock
    private collocOffreRepo repo;

    @Mock
    private collocOffreImageRepo imageRepo;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private collocOffreServiceImpl service;

    private collocOffre sampleOffre;

    @BeforeEach
    void setUp() {
        sampleOffre = new collocOffre();
        sampleOffre.setId(1L);
        sampleOffre.setTitre("Studio Tunis Centre");
        sampleOffre.setDescription("Beau studio meublé au centre-ville");
        sampleOffre.setPrixLoc(450.0);
        sampleOffre.setVille("Tunis");
        sampleOffre.setChambres(2);
        sampleOffre.setMeublee(true);
        sampleOffre.setLatitude(36.8065);
        sampleOffre.setLongitude(10.1815);
        sampleOffre.setOwnerId(100L);
        sampleOffre.setCreatedAt(LocalDate.of(2026, 4, 1));
        sampleOffre.setExpiryDate(LocalDate.of(2026, 6, 1));
        sampleOffre.setNotified(false);
    }

    // =========================================================================
    // CREATE
    // =========================================================================
    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("should create offer without images and set ownerId & createdAt")
        void create_withoutImages_shouldSaveAndReturnOffer() {
            // Arrange
            collocOffre input = new collocOffre();
            input.setTitre("Appartement Sousse");
            input.setDescription("3 chambres, vue mer");
            input.setPrixLoc(600.0);
            input.setVille("Sousse");
            input.setChambres(3);
            input.setMeublee(false);
            input.setLatitude(35.8288);
            input.setLongitude(10.5965);
            input.setExpiryDate(LocalDate.of(2026, 7, 1));

            Long userId = 42L;

            when(repo.save(any(collocOffre.class))).thenAnswer(invocation -> {
                collocOffre saved = invocation.getArgument(0);
                saved.setId(10L);
                return saved;
            });

            // Act
            collocOffre result = service.create(input, null, userId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getTitre()).isEqualTo("Appartement Sousse");
            assertThat(result.getOwnerId()).isEqualTo(userId);
            assertThat(result.getCreatedAt()).isEqualTo(LocalDate.now());
            assertThat(result.getExpiryDate()).isEqualTo(LocalDate.of(2026, 7, 1));

            // Verify repo.save was called exactly once
            ArgumentCaptor<collocOffre> captor = ArgumentCaptor.forClass(collocOffre.class);
            verify(repo, times(1)).save(captor.capture());
            collocOffre captured = captor.getValue();
            assertThat(captured.getOwnerId()).isEqualTo(42L);
            assertThat(captured.getVille()).isEqualTo("Sousse");
            assertThat(captured.getPrixLoc()).isEqualTo(600.0);

            // No image interactions
            verifyNoInteractions(imageRepo);
        }

        @Test
        @DisplayName("should create offer with empty image list (no image processing)")
        void create_withEmptyImageList_shouldNotProcessImages() {
            // Arrange
            collocOffre input = new collocOffre();
            input.setTitre("Test");
            input.setDescription("Desc");
            input.setPrixLoc(300.0);
            input.setVille("Sfax");
            input.setChambres(1);
            input.setMeublee(true);
            input.setLatitude(34.74);
            input.setLongitude(10.76);
            input.setExpiryDate(LocalDate.of(2026, 8, 1));

            when(repo.save(any(collocOffre.class))).thenAnswer(invocation -> {
                collocOffre saved = invocation.getArgument(0);
                saved.setId(20L);
                return saved;
            });

            // Act
            collocOffre result = service.create(input, Collections.emptyList(), 99L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(20L);
            verify(repo, times(1)).save(any(collocOffre.class));
            verifyNoInteractions(imageRepo);
        }

        @Test
        @DisplayName("should correctly map all fields from input to new offer")
        void create_shouldMapAllFieldsCorrectly() {
            // Arrange
            collocOffre input = new collocOffre();
            input.setTitre("Titre");
            input.setDescription("Description");
            input.setPrixLoc(999.99);
            input.setVille("Monastir");
            input.setChambres(5);
            input.setMeublee(false);
            input.setLatitude(35.7643);
            input.setLongitude(10.8113);
            input.setExpiryDate(LocalDate.of(2026, 12, 31));

            when(repo.save(any(collocOffre.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            collocOffre result = service.create(input, null, 7L);

            // Assert — every field is transferred
            assertThat(result.getTitre()).isEqualTo("Titre");
            assertThat(result.getDescription()).isEqualTo("Description");
            assertThat(result.getPrixLoc()).isEqualTo(999.99);
            assertThat(result.getVille()).isEqualTo("Monastir");
            assertThat(result.getChambres()).isEqualTo(5);
            assertThat(result.getMeublee()).isFalse();
            assertThat(result.getLatitude()).isEqualTo(35.7643);
            assertThat(result.getLongitude()).isEqualTo(10.8113);
            assertThat(result.getOwnerId()).isEqualTo(7L);
            assertThat(result.getExpiryDate()).isEqualTo(LocalDate.of(2026, 12, 31));
        }

        @Test
        @DisplayName("should upload images and save image entities with correct metadata")
        void create_withImages_shouldUploadAndSaveImageEntities() throws Exception {
            // Arrange
            collocOffre input = new collocOffre();
            input.setTitre("Offer with Images");
            
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getContentType()).thenReturn("image/png");
            
            List<MultipartFile> images = List.of(file);
            Long userId = 1L;

            when(repo.save(any(collocOffre.class))).thenAnswer(invocation -> {
                collocOffre saved = invocation.getArgument(0);
                saved.setId(100L);
                return saved;
            });

            when(fileStorageService.store(any(MultipartFile.class))).thenReturn("uploaded_test.png");

            // Act
            collocOffre result = service.create(input, images, userId);

            // Assert
            assertThat(result.getId()).isEqualTo(100L);
            verify(fileStorageService).store(any(MultipartFile.class));
            
            ArgumentCaptor<collocOffreImage> imageCaptor = ArgumentCaptor.forClass(collocOffreImage.class);
            verify(imageRepo).save(imageCaptor.capture());
            
            collocOffreImage savedImage = imageCaptor.getValue();
            assertThat(savedImage.getFilename()).isEqualTo("uploaded_test.png");
            assertThat(savedImage.getUrl()).isEqualTo("http://localhost:9092/api/collocation/imagesColloc/uploaded_test.png");
            assertThat(savedImage.getOffre().getId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("should skip image processing for empty files")
        void create_withEmptyMultipartFile_shouldSkipProcessing() {
            // Arrange
            collocOffre input = new collocOffre();
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(true);
            
            List<MultipartFile> images = List.of(file);

            when(repo.save(any(collocOffre.class))).thenReturn(input);

            // Act
            service.create(input, images, 1L);

            // Assert
            verifyNoInteractions(fileStorageService);
            verifyNoInteractions(imageRepo);
        }

        @Test
        @DisplayName("should throw ImageStorageException when storage service fails")
        void create_whenStorageServiceThrowsIOException_shouldThrowException() throws Exception {
            // Arrange
            collocOffre input = new collocOffre();
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getContentType()).thenReturn("image/png");
            when(file.getOriginalFilename()).thenReturn("test.png");
            
            List<MultipartFile> images = List.of(file);

            when(repo.save(any(collocOffre.class))).thenReturn(input);
            when(fileStorageService.store(any(MultipartFile.class))).thenThrow(new IOException("Disk failure"));

            // Act & Assert
            assertThatThrownBy(() -> service.create(input, images, 1L))
                    .isInstanceOf(ImageStorageException.class)
                    .hasMessageContaining("Failed to store image");
        }

        @Test
        @DisplayName("should throw exception for unsupported image type")
        void create_withUnsupportedImageType_shouldThrowException() {
            // Arrange
            collocOffre input = new collocOffre();
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getContentType()).thenReturn("application/pdf");
            
            List<MultipartFile> images = List.of(file);

            // Act & Assert
            assertThatThrownBy(() -> service.create(input, images, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unsupported file type");
            
            verifyNoInteractions(imageRepo);
            verifyNoInteractions(fileStorageService);
        }
    }

    // =========================================================================
    // GET ALL
    // =========================================================================
    @Nested
    @DisplayName("getAll()")
    class GetAllTests {

        @Test
        @DisplayName("should return all offers from repository")
        void getAll_shouldReturnAllOffers() {
            // Arrange
            collocOffre offre2 = new collocOffre();
            offre2.setId(2L);
            offre2.setTitre("Villa Hammamet");

            when(repo.findAll()).thenReturn(List.of(sampleOffre, offre2));

            // Act
            List<collocOffre> result = service.getAll();

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getTitre()).isEqualTo("Studio Tunis Centre");
            assertThat(result.get(1).getTitre()).isEqualTo("Villa Hammamet");
            verify(repo, times(1)).findAll();
        }

        @Test
        @DisplayName("should return empty list when no offers exist")
        void getAll_whenEmpty_shouldReturnEmptyList() {
            when(repo.findAll()).thenReturn(Collections.emptyList());

            List<collocOffre> result = service.getAll();

            assertThat(result).isEmpty();
            verify(repo, times(1)).findAll();
        }
    }

    // =========================================================================
    // GET BY ID
    // =========================================================================
    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("should return offer when found")
        void getById_whenExists_shouldReturnOffer() {
            when(repo.findById(1L)).thenReturn(Optional.of(sampleOffre));

            collocOffre result = service.getById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitre()).isEqualTo("Studio Tunis Centre");
            verify(repo, times(1)).findById(1L);
        }

        @Test
        @DisplayName("should throw OfferNotFoundException when offer not found")
        void getById_whenNotExists_shouldThrowException() {
            when(repo.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getById(999L))
                    .isInstanceOf(OfferNotFoundException.class)
                    .hasMessageContaining("Offer not found with id 999");

            verify(repo, times(1)).findById(999L);
        }
    }

    // =========================================================================
    // FIND BY OWNER ID
    // =========================================================================
    @Nested
    @DisplayName("findByOwnerId()")
    class FindByOwnerIdTests {

        @Test
        @DisplayName("should return offers belonging to the owner")
        void findByOwnerId_shouldReturnOwnerOffers() {
            collocOffre offre2 = new collocOffre();
            offre2.setId(2L);
            offre2.setOwnerId(100L);

            when(repo.findByOwnerId(100L)).thenReturn(List.of(sampleOffre, offre2));

            List<collocOffre> result = service.findByOwnerId(100L);

            assertThat(result)
                    .hasSize(2)
                    .allSatisfy(o -> assertThat(o.getOwnerId()).isEqualTo(100L));
            verify(repo, times(1)).findByOwnerId(100L);
        }

        @Test
        @DisplayName("should return empty list when owner has no offers")
        void findByOwnerId_whenNoOffers_shouldReturnEmptyList() {
            when(repo.findByOwnerId(555L)).thenReturn(Collections.emptyList());

            List<collocOffre> result = service.findByOwnerId(555L);

            assertThat(result).isEmpty();
        }
    }

    // =========================================================================
    // UPDATE OFFRE
    // =========================================================================
    @Nested
    @DisplayName("updateOffre()")
    class UpdateOffreTests {

        @Test
        @DisplayName("should update all mutable fields and preserve immutable ones")
        void updateOffre_shouldUpdateMutableFields() {
            // Arrange
            when(repo.findById(1L)).thenReturn(Optional.of(sampleOffre));
            when(repo.save(any(collocOffre.class))).thenAnswer(invocation -> invocation.getArgument(0));

            collocOffre updatedData = new collocOffre();
            updatedData.setTitre("Updated Title");
            updatedData.setDescription("Updated Description");
            updatedData.setPrixLoc(800.0);
            updatedData.setVille("Sfax");
            updatedData.setChambres(4);
            updatedData.setMeublee(false);
            updatedData.setLatitude(34.74);
            updatedData.setLongitude(10.76);
            updatedData.setExpiryDate(LocalDate.of(2026, 9, 15));

            // Act
            collocOffre result = service.updateOffre(1L, updatedData);

            // Assert — mutable fields updated
            assertThat(result.getTitre()).isEqualTo("Updated Title");
            assertThat(result.getDescription()).isEqualTo("Updated Description");
            assertThat(result.getPrixLoc()).isEqualTo(800.0);
            assertThat(result.getVille()).isEqualTo("Sfax");
            assertThat(result.getChambres()).isEqualTo(4);
            assertThat(result.getMeublee()).isFalse();
            assertThat(result.getLatitude()).isEqualTo(34.74);
            assertThat(result.getLongitude()).isEqualTo(10.76);
            assertThat(result.getExpiryDate()).isEqualTo(LocalDate.of(2026, 9, 15));

            // Assert — immutable fields preserved
            assertThat(result.getOwnerId()).isEqualTo(100L);
            assertThat(result.getCreatedAt()).isEqualTo(LocalDate.of(2026, 4, 1));

            verify(repo).findById(1L);
            verify(repo).save(any(collocOffre.class));
        }

        @Test
        @DisplayName("should throw OfferNotFoundException when offer to update not found")
        void updateOffre_whenNotExists_shouldThrowException() {
            when(repo.findById(999L)).thenReturn(Optional.empty());

            collocOffre updatedData = new collocOffre();
            updatedData.setTitre("Whatever");

            assertThatThrownBy(() -> service.updateOffre(999L, updatedData))
                    .isInstanceOf(OfferNotFoundException.class)
                    .hasMessageContaining("Offre not found with id : 999");

            verify(repo, never()).save(any());
        }
    }

    // =========================================================================
    // DELETE OFFRE
    // =========================================================================
    @Nested
    @DisplayName("deleteOffre()")
    class DeleteOffreTests {

        @Test
        @DisplayName("should delete offer when it exists")
        void deleteOffre_whenExists_shouldDelete() {
            when(repo.findById(1L)).thenReturn(Optional.of(sampleOffre));

            service.deleteOffre(1L);

            verify(repo).findById(1L);
            // Use explicit cast to resolve ambiguity with JpaSpecificationExecutor.delete(DeleteSpecification)
            verify(repo).delete((collocOffre) eq(sampleOffre));
        }

        @Test
        @DisplayName("should throw OfferNotFoundException when offer to delete not found")
        void deleteOffre_whenNotExists_shouldThrowException() {
            when(repo.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteOffre(999L))
                    .isInstanceOf(OfferNotFoundException.class)
                    .hasMessageContaining("Offre not found with id : 999");

            verify(repo, never()).delete((collocOffre) any());
        }
    }

    // =========================================================================
    // SEARCH (with Specification + Pageable)
    // =========================================================================
    @Nested
    @DisplayName("search()")
    class SearchTests {

        @Test
        @DisplayName("should delegate to repo.findAll with specification and pageable")
        @SuppressWarnings("unchecked")
        void search_shouldReturnFilteredPage() {
            // Arrange
            collocOffreFilter filter = new collocOffreFilter();
            filter.setVille("Tunis");
            filter.setMeublee(true);

            Pageable pageable = PageRequest.of(0, 10);
            Page<collocOffre> expectedPage = new PageImpl<>(List.of(sampleOffre), pageable, 1);

            when(repo.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

            // Act
            Page<collocOffre> result = service.search(filter, pageable);

            // Assert
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getVille()).isEqualTo("Tunis");
            verify(repo).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("should return empty page when no matches")
        @SuppressWarnings("unchecked")
        void search_whenNoMatch_shouldReturnEmptyPage() {
            collocOffreFilter filter = new collocOffreFilter();
            filter.setVille("NonExistentCity");

            Pageable pageable = PageRequest.of(0, 5);
            Page<collocOffre> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(repo.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

            Page<collocOffre> result = service.search(filter, pageable);

            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getContent()).isEmpty();
        }
    }

    // =========================================================================
    // GET NEARBY OFFERS
    // =========================================================================
    @Nested
    @DisplayName("getNearbyOffers()")
    class GetNearbyOffersTests {

        @Test
        @DisplayName("should return nearby offers within radius")
        void getNearbyOffers_shouldReturnOffersWithinRadius() {
            double lat = 36.8065;
            double lng = 10.1815;
            double radius = 5.0;

            when(repo.findNearbyOffers(lat, lng, radius)).thenReturn(List.of(sampleOffre));

            List<collocOffre> result = service.getNearbyOffers(lat, lng, radius);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitre()).isEqualTo("Studio Tunis Centre");
            verify(repo).findNearbyOffers(lat, lng, radius);
        }

        @Test
        @DisplayName("should return empty list when no offers nearby")
        void getNearbyOffers_whenNone_shouldReturnEmptyList() {
            when(repo.findNearbyOffers(anyDouble(), anyDouble(), anyDouble()))
                    .thenReturn(Collections.emptyList());

            List<collocOffre> result = service.getNearbyOffers(0.0, 0.0, 1.0);

            assertThat(result).isEmpty();
        }
    }
}
