package avvillas.template_admin.util;

import avvillas.template_admin.service.dto.FieldChange;
import lombok.Data;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Data
@AllArgsConstructor
class TestObject {
    @SuppressWarnings("unused")
    private String field1;
    @SuppressWarnings("unused")
    private Integer field2;
    @SuppressWarnings("unused")
    private Boolean field3;
}

class CalculateFieldChangesTest {

    @Test
    @DisplayName("Debe detectar un cambio en un campo simple")
    void shouldDetectChangeInSimpleField() {
        TestObject source = new TestObject("value1", 1, true);
        TestObject target = new TestObject("value2", 1, true);

        List<FieldChange> changes = CalculateFieldChanges.calculate(source, target);

        assertEquals(1, changes.size());
        FieldChange change = changes.get(0);
        assertEquals("field1", change.getFieldName());
        assertEquals("value1", change.getPreviousValue());
        assertEquals("value2", change.getNewValue());
    }

    @Test
    @DisplayName("Debe detectar múltiples cambios en diferentes campos")
    void shouldDetectMultipleFieldChanges() {
        TestObject source = new TestObject("value1", 1, true);
        TestObject target = new TestObject("value2", 2, false);

        List<FieldChange> changes = CalculateFieldChanges.calculate(source, target);

        assertEquals(3, changes.size());

        FieldChange change1 = changes.get(0);
        assertEquals("field1", change1.getFieldName());
        assertEquals("value1", change1.getPreviousValue());
        assertEquals("value2", change1.getNewValue());

        FieldChange change2 = changes.get(1);
        assertEquals("field2", change2.getFieldName());
        assertEquals(1, change2.getPreviousValue());
        assertEquals(2, change2.getNewValue());

        FieldChange change3 = changes.get(2);
        assertEquals("field3", change3.getFieldName());
        assertEquals(true, change3.getPreviousValue());
        assertEquals(false, change3.getNewValue());
    }


    @Test
    @DisplayName("Debe ignorar campos sin cambios")
    void shouldIgnoreUnchangedFields() {
        TestObject source = new TestObject("value1", 1, true);
        TestObject target = new TestObject("value1", 1, true);

        List<FieldChange> changes = CalculateFieldChanges.calculate(source, target);

        assertTrue(changes.isEmpty());
    }
}