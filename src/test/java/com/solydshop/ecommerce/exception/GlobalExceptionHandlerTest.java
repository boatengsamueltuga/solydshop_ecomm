package com.solydshop.ecommerce.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Mock private MethodParameter methodParameter;
    @Mock private BindingResult bindingResult;

    @Test
    void handleValidation_singleFieldError_producesReadableMessage() {
        FieldError fieldError = new FieldError("sellerDowngradeRequestPayload", "reason",
                "Reason must be at least 10 characters");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);

        assertEquals(400, response.getStatusCode().value());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Bad Request", response.getBody().get("error"));
        assertEquals("reason: Reason must be at least 10 characters", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleValidation_multipleFieldErrors_joinsThemReadably() {
        FieldError first  = new FieldError("payload", "email", "must be a valid email");
        FieldError second = new FieldError("payload", "businessName", "must not be blank");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(first, second));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);

        assertEquals("email: must be a valid email; businessName: must not be blank",
                response.getBody().get("message"));
    }

    @Test
    void handleResourceNotFound_producesConsistentShape() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Order not found");

        ResponseEntity<Map<String, Object>> response = handler.handleResourceNotFound(ex);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Not Found", response.getBody().get("error"));
        assertEquals("Order not found", response.getBody().get("message"));
    }

    @Test
    void handleResponseStatus_producesConsistentShape() {
        ResponseStatusException ex = new ResponseStatusException(
                org.springframework.http.HttpStatus.CONFLICT, "Already reviewed");

        ResponseEntity<Map<String, Object>> response = handler.handleResponseStatus(ex);

        assertEquals(409, response.getStatusCode().value());
        assertEquals("Already reviewed", response.getBody().get("message"));
    }
}
