package avvillas.template_admin.constant.message;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class ObjectMessage {
    public static final String OBJECT = "Objeto %s";
    public static final String OBJECT_DELETED = String.format(OBJECT, "eliminado.");
    public static final String OBJECT_CREATED = String.format(OBJECT, "creado.");
    public static final String OBJECT_UPDATED = String.format(OBJECT, "modificado.");
    public static final String OBJECT_NOT_FOUND = String.format(OBJECT, "no encontrado.");
    public static final String OBJECT_ALREADY_EXIST = "Ya existe un objeto con el identificador: %s.";

    public static final String NAME_REQUIRED = "El campo 'nombre' es obligatorio.";
    public static final String NAME_MIN_MAX = "El campo 'nombre' debe tener como mínimo 1 carácter y máximo 100 caracteres.";

    public static final String IDENTIFIER_REQUIRED = "El campo 'identifier' es obligatorio.";
    public static final String IDENTIFIER_MIN_MAX = "El campo 'identifier' debe tener como mínimo 1 carácter y máximo 10 caracteres.";

    public static final String CONTENT_REQUIRED = "El campo 'content' es obligatorio.";
    public static final String HEADER_REQUIRED = "El campo 'header' es obligatorio en el tipo tabla.";
    public static final String HEADER_MIN = "El encabezado debe tener al menos una columna.";

    public static final String BODY_REQUIRED = "El campo 'body' es obligatorio en el tipo tabla.";
    public static final String FIXED_REQUIRED = "El campo 'fixed' es obligatorio en el tipo tabla.";
    public static final String ROW_INDEX_REQUIRED = "El campo 'rowIndex' es obligatorio en el tipo tabla.";
    public static final String COLUMN_INDEX_REQUIRED = "El campo 'columnIndex' es obligatorio en el tipo tabla.";
    public static final String CELL_NOT_EXIST = "Celda con Id %s no econtrada para actualizar/eliminar.";

    public static final String CHANGE_TYPE_REQUIRED_FULL = "Para cambiar al tipo %s se requiere la información completa del tipo de objeto.";
    public static final String VERIFY_FORMAT_DATA = "Se esperaban las propiedades del tipo: %s.";

    public static final String CONTENT_REQUIRED_BY_TYPE = "El contenido es obligatorio para objetos de tipo: %s.";
    public static final String TYPE_NOT_SUPPORTED = "Tipo no soportado: %s.";
    public static final String TABLE_FIXED_CELLS_INVALID = "Si se envían celdas, 'fixed' debe ser 'true'.";
    public static final String NAME_IDENTIFIER_REQUIRED = "El nombre y el identificador son obligatorios.";

    public static final String IMAGE_REQUIRED = "La imagen es obligatoria.";
    public static final String IMAGE_NOT_SUPPORTED_FORMAT = "Formato de imagen no soportado.";
    public static final String IMAGE_MAX_SIZE = "La imagen excede el tamaño máximo de 200KB.";
    public static final String OBJECT_EXIST_IN_FORMAT = "No se puede eliminar el registro porque tiene una asociación en Presentacion en modo diseño del Formato PDF";

    public static final String TYPE_REQUIRED = "El campo 'type' es obligatorio.";
    public static final String ALLOWED_TYPES = "TEXT|IMAGE|SHAPE|TABLE";
    public static final String ALLOWED_TYPES_READABLE = "Tipo de objeto inválido. Debe ser: TEXT, IMAGE, SHAPE o TABLE.";
    public static final String OBJECT_TYPE_REQUIRED = "El campo 'objectType' es obligatorio.";
    public static final String TABLE_CLOSURE_REQUIRED = "El campo 'esCierreTabla' es obligatorio y debe ser Activo o Inactivo.";
    public static final String CONDITIONAL_RENDER_FIELD_REQUIRED = "El campo de evaluación es obligatorio cuando la visibilidad condicional está activa.";
    public static final String CONDITIONAL_RENDER_VALUE_REQUIRED = "El valor esperado es obligatorio cuando la visibilidad condicional está activa.";
    public static final String CONDITIONAL_RENDER_FIELD_PRODUCT_INVALID = "El campo de evaluación no pertenece al producto seleccionado.";
    public static final String CONDITIONAL_RENDER_OPERATOR_INVALID = "Operador de visibilidad condicional inválido. Valor permitido: EQUALS.";
    public static final String CONDITIONAL_RENDER_OPERATOR_EQUALS = "EQUALS";

    public static final String TABLE_TYPE = "TABLE";
    public static final String TYPE = "type";
    public static final String TEXT_TYPE = "TEXT";
    public static final String IMAGE_TYPE = "IMAGE";
    public static final String SHAPE_TYPE = "SHAPE";
    public static final String GENERIC_TYPE = "GENERIC";
    public static final String HANDLER = "Handler";
    public static final String OBJECT_TYPE = "objectType";
    public static final String REGEX_BASE64_IMAGE = "^data:image/(png|jpeg|jpg);base64,[a-zA-Z0-9+/]+={0,2}";

    public static final int MAX_BASE64_LENGTH = 273_067;
    public static final int MAX_SIZE_BYTES = 200_000;
}
