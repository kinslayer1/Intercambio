package avvillas.template_admin.util;

import jakarta.annotation.Nullable;

public class ConversionUtil {

    private ConversionUtil() {}

    public static Integer booleanToInteger(Boolean value) {
        if (value == null) return null;
        return value ? 1 : 0;
    }

    @Nullable
    public static Boolean integerToBoolean(Integer value) {
        if (value == null) return null;
        return value == 1;
    }
}