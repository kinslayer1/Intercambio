package avvillas.template_admin.web.controller.object.Helpers;

import avvillas.template_admin.constant.message.ObjectMessage;
import avvillas.template_admin.service.implementations.ObjectServiceImp.helpers.BlobConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Blob;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BlobConverterTest {
    private final BlobConverter converter = new BlobConverter();

    @Test
    @DisplayName("convertToDatabaseColumn - debería retornar null si se envía null")
    void convertToDatabaseColumn_returnsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    @DisplayName("convertToEntityAttribute - debería lanzar IllegalArgumentException si ocurre SQLException")
    void convertToEntityAttribute_throwsExceptionOnSQLException() throws Exception {
        Blob blob = mock(Blob.class);
        when(blob.length()).thenThrow(new SQLException("Error simulado"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            converter.convertToEntityAttribute(blob);
        });

        assertEquals(ObjectMessage.IMAGE_NOT_SUPPORTED_FORMAT, exception.getMessage());
        assertInstanceOf(SQLException.class, exception.getCause());
    }
    
}
