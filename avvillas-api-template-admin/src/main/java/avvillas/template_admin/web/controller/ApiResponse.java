package avvillas.template_admin.web.controller;

import avvillas.template_admin.service.dto.FieldChange;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@SuppressWarnings("unused")
public class ApiResponse<T> {
    private final T data;
    private final List<String> message;
    private static final String SERVICE = "TEMPLATE_ADMIN";
    private final List<FieldChange> logEntries;

    @JsonProperty("service")
    public String getService() { return SERVICE;}

    private ApiResponse(T data, List<String> message, List<FieldChange> log) {
        this.data = data;
        this.message = message;
        this.logEntries = log;
    }

    public static <T> ApiResponse<T> success(T data, String message, List<FieldChange> log) {
        return new ApiResponse<>(
                data,
                Collections.singletonList(message),
                log
        );
    }


    public static <T> ApiResponse<T> error(List<String> errorMessages) {
        return new ApiResponse<>(
                null,
                errorMessages,
                null
        );
    }

    public static <T> ApiResponse<T> error(String errorMessage) {
        return new ApiResponse<>(
                null,
                Collections.singletonList(errorMessage),
                null
        );
    }
}