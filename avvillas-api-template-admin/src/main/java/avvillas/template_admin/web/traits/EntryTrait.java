package avvillas.template_admin.web.traits;

import avvillas.template_admin.constant.MessageConstant;
import avvillas.template_admin.constant.message.FormatMessage;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.NoSuchElementException;

@Component
@AllArgsConstructor
public class EntryTrait {

    private static final String RESOURCE_PATH_TEMPLATE = "input-product-structure/";
    private static final String SERVICE = "loader";
    private final CommunicationBetweenServices communication;

    public void getEntryById(String id) {
        try {
            String resource = RESOURCE_PATH_TEMPLATE + id;

            communication.communicateWithMicroservice(
                    SERVICE,
                    resource,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );

        } catch (HttpClientErrorException e) {
            handleHttpClientErrorException(e, id);
        }
    }

    private void handleHttpClientErrorException(HttpClientErrorException e, String id) {
        String errorResponseBody = e.getResponseBodyAsString();
        String errorMessage = MessageConstant.format(FormatMessage.ENTRY_NOT_FOUND, id);

        try {
            errorMessage = parseErrorResponse(errorResponseBody);
        } catch (Exception ex) {
            System.err.println("Error parsing response: " + ex.getMessage());
        }
        throw new NoSuchElementException(errorMessage);
    }

    private String parseErrorResponse(String responseBody) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        ErrorResponse errorResponse = objectMapper.readValue(responseBody, ErrorResponse.class);
        return errorResponse.getMessage().get(0);
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ErrorResponse {
        private List<String> message;
    }
}