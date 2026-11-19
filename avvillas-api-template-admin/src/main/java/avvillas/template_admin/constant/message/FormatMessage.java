package avvillas.template_admin.constant.message;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FormatMessage {
    public static final String FORMAT = "Formato %s";

    public static final String FORMAT_CREATED = String.format(FORMAT, "Creado.");
    public static final String FORMAT_UPDATED = String.format(FORMAT, "Actualizado.");
    public static final String FORMAT_ACTIVE = String.format(FORMAT, "Activado.");

    public static final String PRODUCT_ID_INVALID = "El campo productId tiene un formato UUID no válido.";
    public static final String OBJECT_ID_INVALID = "El campo objectId tiene un formato UUID no válido.";
    public static final String FIELD_ID_INVALID = "El campo fieldId tiene un formato UUID no válido.";

    public static final String FORMAT_NOT_FOUND = "Formato no encontrado con el id: %s.";
    public static final String OBJECT_NOT_FOUND = "Objeto no encontrado con el id: %s.";
    public static final String ENTRY_NOT_FOUND = "Entrada no encontrada con el id: %s.";

    public static final String ERROR_UPDATE_FORMAT = "No se puede inactivar el formato del producto seleccionado.";
    public static final String ERROR_GET_FORMAT = "Producto sin plantilla configurada.";

    public static final String PRODUCT_ID_REQUIRED = "El campo productId es obligatorio.";
    public static final String PAPER_TYPE_REQUIRED = "El campo paperType es obligatorio.";
    public static final String FONT_FAMILY_REQUIRED = "El campo fontFamily es obligatorio.";
    public static final String PAGES_REQUIRED = "El campo pages es obligatorio.";
    public static final String MARGINS_REQUIRED = "El campo margins es obligatorio.";
    public static final String ACTIVE_REQUIRED = "El campo active es obligatorio.";
    public static final String PAGE_NUMBER_REQUIRED = "El campo pageNumber es obligatorio.";
    public static final String PDF_CONFIG_REQUIRED = "El campo pdfConfig es obligatorio.";
    public static final String ELEMENTS_REQUIRED = "La lista de elementos (elements) es obligatoria y no puede estar vacía.";
    public static final String OBJECT_ID_ELEMENTS_REQUIRED = "El campo objectId de la lista de elementos (elements) es obligatorio.";
    public static final String POSITION_X_ELEMENTS_REQUIRED = "El campo positionX de la lista de elementos (elements) es obligatorio.";
    public static final String POSITION_Y_ELEMENTS_REQUIRED = "El campo positionY de la lista de elementos (elements) es obligatorio.";
    public static final String FIELDS_REQUIRED = "La lista de campos (fields) es obligatoria y no puede estar vacía.";
    public static final String FIELD_ID_FIELDS_REQUIRED = "El campo fieldId de la lista de campos (fields) es obligatorio.";
    public static final String POSITION_X_FIELDS_REQUIRED = "El campo positionX de la lista de campos (fields) es obligatorio.";
    public static final String POSITION_Y_FIELDS_REQUIRED = "El campo positionY de la lista de campos (fields) es obligatorio.";
}