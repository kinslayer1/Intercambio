package avvillas.template_admin.util;

import java.util.Map;

public class PdfUtil {
    private PdfUtil() {}

    public static String parseStyles(Map<String, Object> styles) {
        StringBuilder css = new StringBuilder();
        for (Map.Entry<String, Object> entry : styles.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            String cssKey = key.replaceAll("([A-Z])", "-$1").toLowerCase();
            if (value != null) {
                String cssValue = value.toString();

                if (cssValue.matches("\\d+")) cssValue += "px";

                css.append(cssKey).append(": ").append(cssValue).append(" !important; ");
            }
        }
        return css.toString();
    }

}