package avvillas.template_admin.service.implementations.ObjectServiceImp.helpers;

import avvillas.template_admin.constant.message.ObjectMessage;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.Blob;
import java.sql.SQLException;

@Converter
public class BlobConverter implements AttributeConverter<byte[], Blob> {

    @Override
    public Blob convertToDatabaseColumn(byte[] bytes) {
        try {
            return bytes != null ? new SerialBlob(bytes) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] convertToEntityAttribute(Blob blob) {
        try {
            return blob != null ? blob.getBytes(1, (int) blob.length()) : null;
        } catch (SQLException e) {
            throw new IllegalArgumentException(ObjectMessage.IMAGE_NOT_SUPPORTED_FORMAT, e);
        }
    }
}
