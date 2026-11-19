package avvillas.template_admin.constant;


public final class MessageConstant {
    private MessageConstant() {}

    public static String format(String message, Object... args) {
        return String.format(message, args);
    }
}
