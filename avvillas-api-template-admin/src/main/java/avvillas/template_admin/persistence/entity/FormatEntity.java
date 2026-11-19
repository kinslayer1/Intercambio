package avvillas.template_admin.persistence.entity;

import avvillas.template_admin.persistence.audit.AuditableEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "formato")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FormatEntity extends AuditableEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "producto_id", length = 36, nullable = false)
    private String productId;

    @Column
    private Integer version = 1;

    @Column(name = "tipo_papel", length = 100)
    private String paperType;

    @Column(name = "continua")
    private Boolean continues = false;

    @Column(name = "fuente", length = 100)
    private String fontFamily;

    @Column(name = "margen_superior", precision = 5, scale = 2)
    private BigDecimal topMargin;

    @Column(name = "margen_inferior", precision = 5, scale = 2)
    private BigDecimal bottomMargin;

    @Column(name = "margen_derecha", precision = 5, scale = 2)
    private BigDecimal rightMargin;

    @Column(name = "margen_izquierda", precision = 5, scale = 2)
    private BigDecimal leftMargin;

    @Column(name = "activo", precision = 1)
    private Integer active = 1;

    @Column(name = "html_content", columnDefinition = "CLOB")
    private String htmlContent;

    @Column(name = "base64_content", columnDefinition = "CLOB")
    private String base64Content;

    @OneToMany(mappedBy = "format", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("pageNumber ASC")
    @JsonManagedReference
    private List<FormatPageEntity> pages = new ArrayList<>();
}