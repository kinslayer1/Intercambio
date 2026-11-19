package avvillas.template_admin.persistence.entity;

import avvillas.template_admin.service.implementations.ObjectServiceImp.helpers.BlobConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "imagen_objeto")
@PrimaryKeyJoinColumn(name = "id")
public class ObjectImageEntity extends ObjectEntity {

    @Lob
    @Column(name = "imagen", columnDefinition = "BLOB")
    @Convert(converter = BlobConverter.class)
    private byte[] image;

    @Column(name = "tipo_mime", length = 50)
    private String mimeType;

}
