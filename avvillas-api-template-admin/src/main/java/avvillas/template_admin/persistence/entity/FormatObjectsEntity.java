package avvillas.template_admin.persistence.entity;

import avvillas.template_admin.constant.fields.ObjectType;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "formato_objetos")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FormatObjectsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    private String id;

    @ManyToOne
    @JoinColumn(name = "objeto_id")
    private ObjectEntity object;

    @Column(name = "campo_id")
    private String fieldId;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "pagina_id", nullable = false)
    private FormatPageEntity page;

    @Column(name = "posicion_y", nullable = false)
    private Integer positionY;

    @Column(name = "posicion_x", nullable = false)
    private Integer positionX;

    @Column(name = "tipo")
    @Enumerated(EnumType.STRING)
    private ObjectType type;
}