package avvillas.template_admin.web.traits;

import avvillas.template_admin.constant.MessageConstant;
import avvillas.template_admin.constant.message.FormatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;

import java.lang.reflect.Method;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("EntryTrait Tests")
class EntryTraitTest {

    @MockitoBean
    private CommunicationBetweenServices communicationBetweenServices;

    @Autowired
    private EntryTrait entryTrait;

    private static final String TEST_ID = "test-id-123";
    private static final String RESOURCE_PATH = "input-product-structure/" + TEST_ID;
    private static final String SERVICE = "loader";

    @BeforeEach
    void setUp() {
        Mockito.reset(communicationBetweenServices);
    }

    private String invokeParseErrorResponse(String responseBody) throws Exception {
        Method parseErrorResponseMethod = EntryTrait.class.getDeclaredMethod("parseErrorResponse", String.class);
        parseErrorResponseMethod.setAccessible(true);
        try {
            return (String) parseErrorResponseMethod.invoke(entryTrait, responseBody);
        } catch (java.lang.reflect.InvocationTargetException e) {
            if (e.getCause() != null) {
                throw (Exception) e.getCause();
            }
            throw e;
        }
    }

    @Test
    @DisplayName("Should throw NoSuchElementException with parsed error message when HttpClientErrorException occurs")
    void shouldThrowNoSuchElementExceptionWithParsedErrorMessage() {
        String errorMessage = "Custom error message";
        String errorResponseBody = "{\"message\":[\"" + errorMessage + "\"]}";

        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                null,
                errorResponseBody.getBytes(),
                null
        );

        when(communicationBetweenServices.communicateWithMicroservice(
                eq(SERVICE),
                eq(RESOURCE_PATH),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenThrow(exception);

        NoSuchElementException thrownException = assertThrows(
                NoSuchElementException.class,
                () -> entryTrait.getEntryById(TEST_ID)
        );

        assertEquals(errorMessage, thrownException.getMessage());
    }

    @Test
    @DisplayName("Should throw NoSuchElementException with default error message when parsing fails")
    void shouldThrowNoSuchElementExceptionWithDefaultMessageWhenParsingFails() {
        String invalidJsonResponse = "This is not valid JSON";
        String defaultErrorMessage = MessageConstant.format(FormatMessage.ENTRY_NOT_FOUND, TEST_ID);

        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                null,
                invalidJsonResponse.getBytes(),
                null
        );

        when(communicationBetweenServices.communicateWithMicroservice(
                eq(SERVICE),
                eq(RESOURCE_PATH),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenThrow(exception);

        NoSuchElementException thrownException = assertThrows(
                NoSuchElementException.class,
                () -> entryTrait.getEntryById(TEST_ID)
        );

        assertEquals(defaultErrorMessage, thrownException.getMessage());
    }

    @Test
    @DisplayName("Should successfully parse error response with valid message")
    void shouldSuccessfullyParseErrorResponseWithValidMessage() throws Exception {
        String expectedMessage = "Test error message";
        String jsonResponse = "{\"message\":[\"" + expectedMessage + "\"]}";

        String result = invokeParseErrorResponse(jsonResponse);

        assertEquals(expectedMessage, result);
    }

    @Test
    @DisplayName("Should throw exception when parsing error response with empty message list")
    void shouldThrowExceptionWhenParsingErrorResponseWithEmptyMessageList() {
        String jsonResponse = "{\"message\":[]}";

       assertThrows(
                IndexOutOfBoundsException.class,
                () -> invokeParseErrorResponse(jsonResponse)
        );
    }

    @Test
    @DisplayName("Should throw exception when parsing error response with null message list")
    void shouldThrowExceptionWhenParsingErrorResponseWithNullMessageList() {
        String jsonResponse = "{\"message\":null}";

       assertThrows(
                NullPointerException.class,
                () -> invokeParseErrorResponse(jsonResponse)
        );
    }

    @Test
    @DisplayName("Should throw exception when parsing invalid JSON")
    void shouldThrowExceptionWhenParsingInvalidJson() {
        String invalidJson = "This is not valid JSON";

        assertThrows(
                Exception.class,
                () -> invokeParseErrorResponse(invalidJson)
        );
    }
}