package avvillas.template_admin.persistence.entity;

import avvillas.template_admin.service.dto.object.TableBodyConfig;
import avvillas.template_admin.service.dto.object.TableHeaderConfig;
import avvillas.template_admin.service.implementations.ObjectServiceImp.helpers.BodyConverter;
import avvillas.template_admin.service.implementations.ObjectServiceImp.helpers.HeaderConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tabla_objeto")
@PrimaryKeyJoinColumn(name = "id")
public class ObjectTableEntity extends ObjectEntity {

    @Convert(converter = HeaderConverter.class)
    @Column(name = "encabezado", columnDefinition = "CLOB")
    private TableHeaderConfig header;

    @Convert(converter = BodyConverter.class)
    @Column(name = "cuerpo", columnDefinition = "CLOB")
    private TableBodyConfig body;

    @Column(name = "estatica", precision = 1)
    private Integer fixed;

    @OneToMany(mappedBy = "tableObject", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<CellEntity> cells;
}
