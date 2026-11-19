package avvillas.template_admin.constant.message;

public class PdfMessage {

    private PdfMessage() {
    }

    public static final String PDF = "Pdf %s";

    public static final String PDF_CREATED = String.format(PDF, "Creado.");

    public static final String ERROR_PDF = "Error al generar el PDF: %s.";

}