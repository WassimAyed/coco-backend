package tn.esprit.collocationservice.Entity;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("collocOffreSpecifications Unit Tests")
class CollocOffreSpecificationsTest {

    @Test
    @DisplayName("filter() should create predicates for all non-null filter fields")
    @SuppressWarnings("unchecked")
    void filter_withAllFields_shouldCreatePredicates() {
        // Arrange
        collocOffreFilter filter = new collocOffreFilter();
        filter.setMinPrixLoc(100.0);
        filter.setMaxPrixLoc(500.0);
        filter.setVille("Tunis");
        filter.setMeublee(true);
        filter.setMinChambres(2);

        Specification<collocOffre> spec = collocOffreSpecifications.filter(filter);

        Root<collocOffre> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Predicate p1 = mock(Predicate.class);
        Predicate p2 = mock(Predicate.class);
        Predicate p3 = mock(Predicate.class);
        Predicate p4 = mock(Predicate.class);
        Predicate p5 = mock(Predicate.class);
        Predicate combined = mock(Predicate.class);

        when(cb.ge(root.get("prixLoc"), 100.0)).thenReturn(p1);
        when(cb.le(root.get("prixLoc"), 500.0)).thenReturn(p2);
        when(cb.equal(root.get("ville"), "Tunis")).thenReturn(p3);
        when(cb.equal(root.get("meublee"), true)).thenReturn(p4);
        when(cb.ge(root.get("chambres"), 2)).thenReturn(p5);
        when(cb.and(any(Predicate[].class))).thenReturn(combined);

        // Act
        Predicate result = spec.toPredicate(root, query, cb);

        // Assert
        assertThat(result).isEqualTo(combined);
        verify(cb).ge(root.get("prixLoc"), 100.0);
        verify(cb).le(root.get("prixLoc"), 500.0);
        verify(cb).equal(root.get("ville"), "Tunis");
        verify(cb).equal(root.get("meublee"), true);
        verify(cb).ge(root.get("chambres"), 2);
    }

    @Test
    @DisplayName("filter() should create empty and predicate when filter is empty")
    @SuppressWarnings("unchecked")
    void filter_withEmptyFilter_shouldReturnEmptyAnd() {
        collocOffreFilter filter = new collocOffreFilter();
        Specification<collocOffre> spec = collocOffreSpecifications.filter(filter);

        Root<collocOffre> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Predicate combined = mock(Predicate.class);

        when(cb.and(any(Predicate[].class))).thenReturn(combined);

        Predicate result = spec.toPredicate(root, query, cb);

        assertThat(result).isEqualTo(combined);
        verify(cb).and(new Predicate[0]);
    }
}
