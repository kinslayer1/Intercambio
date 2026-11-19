package avvillas.template_admin.persistence.entity;

import avvillas.template_admin.persistence.audit.AuditableEntity;
import avvillas.template_admin.persistence.converter.BooleanToIntegerConverter;
import avvillas.template_admin.service.implementations.ObjectServiceImp.helpers.StylePropertiesConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;


@Entity
@Table(name = "objeto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@EqualsAndHashCode(callSuper = true)
public class ObjectEntity extends AuditableEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "producto_id", length = 36, nullable = false)
    private String productId;

    @Column(name = "nombre", length = 100)
    private String name;

    @Column(name = "identificador", length = 10)
    private String identifier;

    @Column(name = "tipo", length = 5)
    private String type;

    @Column(name = "contenido", length = 4000, columnDefinition = "VARCHAR2(4000)")
    private String content;

    @Convert(converter = BooleanToIntegerConverter.class)
    @Column(name = "es_cierre_tabla", precision = 1, scale = 0)
    private Boolean esCierreTabla;

    @Convert(converter = BooleanToIntegerConverter.class)
    @Column(name = "render_condicional_habilitado", columnDefinition = "NUMBER(1) DEFAULT 0 NOT NULL")
    private Boolean renderCondicionalHabilitado = false;

    @Column(name = "render_condicional_campo_id", length = 36)
    private String renderCondicionalCampoId;

    @Column(name = "render_condicional_operador", length = 20, columnDefinition = "VARCHAR2(20) DEFAULT 'EQUALS' NOT NULL")
    private String renderCondicionalOperador = "EQUALS";

    @Column(name = "render_condicional_valor_esperado", length = 200, columnDefinition = "VARCHAR2(200)")
    private String renderCondicionalValorEsperado;

    @Convert(converter = StylePropertiesConverter.class)
    @Column(name = "propiedades_estilo", columnDefinition = "CLOB")
    private Map<String, Object> styleProperties;
}

