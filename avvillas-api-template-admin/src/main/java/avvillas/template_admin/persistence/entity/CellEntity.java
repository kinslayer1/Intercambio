package avvillas.template_admin.persistence.entity;

import avvillas.template_admin.persistence.audit.AuditableEntity;
import avvillas.template_admin.service.implementations.ObjectServiceImp.helpers.StylePropertiesConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "celda")
public class CellEntity extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JoinColumn(name = "tabla_objecto_id", nullable = false)
    private ObjectTableEntity tableObject;

    @Column(name = "estructura_entrada_id", length = 36)
    private String inputStructureProductId;

    @Column(name = "estatico", precision = 1)
    private Integer fixed;

    @Column(name = "fila_indice")
    private Integer rowIndex;

    @Column(name = "columna_indice")
    private Integer columnIndex;

    @Column(name = "col-span")
    private Integer colSpan;

    @Column(name = "row-span")
    private Integer rowSpan;

    @Convert(converter = StylePropertiesConverter.class)
    @Column(name = "propiedades_estilos", columnDefinition = "CLOB")
    private Map<String, Object> styleProperties;

    @Column(name = "contenido", length = 4000)
    private String content;
}
