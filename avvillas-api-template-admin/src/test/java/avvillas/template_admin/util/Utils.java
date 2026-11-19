package avvillas.template_admin.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Utils {
    public static String asJsonString(Object obj, ObjectMapper mapper) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON serialization error", e);
        }
    }
}