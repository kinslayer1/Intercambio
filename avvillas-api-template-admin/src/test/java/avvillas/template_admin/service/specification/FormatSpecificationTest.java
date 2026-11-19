package avvillas.template_admin.service.specification;

import avvillas.template_admin.constant.fields.FormatFieldNames;
import avvillas.template_admin.constant.fields.ObjectFieldNames;
import avvillas.template_admin.persistence.entity.FormatEntity;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class FormatSpecificationTest {

    @Test
    @DisplayName("Debe retornar una especificación válida cuando los productIds no están vacíos")
    void shouldReturnSpecificationWhenProductIdsAreProvided() {
        List<String> productIds = List.of("prod-1", "prod-2");

        Root<FormatEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> path = mock(Path.class);

        when(root.get(ObjectFieldNames.PRODUCT_ID.getFieldName())).thenReturn(path);
        when(path.in(productIds)).thenReturn(mock(Predicate.class));

        var spec = FormatSpecification.filterByProductIds(productIds);
        Predicate predicate = spec.toPredicate(root, query, cb);

        verify(root, times(1)).get(ObjectFieldNames.PRODUCT_ID.getFieldName());
        verify(path, times(1)).in(productIds);
        assertNotNull(predicate);
    }

    @Test
    @DisplayName("Debe retornar disjunction cuando productIds son nulos o vacíos")
    void shouldReturnDisjunctionWhenProductIdsAreNullOrEmpty() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        when(cb.disjunction()).thenReturn(mock(Predicate.class));

        var specWithNull = FormatSpecification.filterByProductIds(null);
        Predicate predicateWithNull = specWithNull.toPredicate(null, null, cb);

        var specWithEmpty = FormatSpecification.filterByProductIds(Collections.emptyList());
        Predicate predicateWithEmpty = specWithEmpty.toPredicate(null, null, cb);

        verify(cb, times(2)).disjunction();
        assertNotNull(predicateWithNull);
        assertNotNull(predicateWithEmpty);
    }

    @Test
    @DisplayName("Debe retornar la especificación para filtrar por versión válida")
    void shouldReturnSpecificationForVersionFilter() {
        String version = "5";

        Root<FormatEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> versionPath = mock(Path.class);

        when(root.get(FormatFieldNames.VERSION.getFieldName())).thenReturn(versionPath);
        when(cb.equal(versionPath, Integer.parseInt(version))).thenReturn(mock(Predicate.class));

        var spec = FormatSpecification.filterByVersion(version);
        Predicate predicate = spec.toPredicate(root, query, cb);

        verify(root, times(1)).get(FormatFieldNames.VERSION.getFieldName());
        verify(cb, times(1)).equal(versionPath, Integer.parseInt(version));
        assertNotNull(predicate);
    }
}