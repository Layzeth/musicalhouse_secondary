package ec.edu.espe.musicalhousesecondary.exceptions;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.apache.tomcat.util.http.fileupload.impl.InvalidContentTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;

import static ec.edu.espe.musicalhousesecondary.exceptions.ErrorResponse.of;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.status;

@Log4j2
@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("Error en GlobalExceptionHandler", ex);
        return badRequest().body(of("Error en el servidor"));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        var constraintViolations = ex.getConstraintViolations();
        var errors = new ValidationErrorResponse();
        constraintViolations.forEach(constraintViolation -> {
            var path = constraintViolation.getPropertyPath().toString().split("\\.");
            var param = path[path.length - 1];
            errors.addError(param, constraintViolation.getMessage());
        });

        return badRequest().body(errors);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        return badRequest().body(of("Falta el parámetro de la solicitud: " + ex.getParameterName()));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestPartException(MissingServletRequestPartException ex) {
        return badRequest().body(of("Falta la parte de la solicitud: " + ex.getRequestPartName()));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        return badRequest().body(of("Falta el encabezado de la solicitud: " + ex.getHeaderName()));
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ErrorResponse> handleMissingPathVariableException(MissingPathVariableException ex) {
        return badRequest().body(of("Falta la variable de ruta: " + ex.getVariableName()));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        return badRequest().body(of("Tipo de medio no soportado: " + ex.getContentType()));
    }

    @ExceptionHandler(InvalidContentTypeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidContentTypeException(InvalidContentTypeException ex) {
        return badRequest().body(of("Tipo de contenido no válido: " + ex.getMessage()));
    }

    @ExceptionHandler(ClientException.class)
    public ResponseEntity<Object> handleClientException(ClientException ex) {
        return status(ex.getHttpStatusCode()).headers(ex.getHeaders()).body(ex.getBody());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {

        var errors = new ValidationErrorResponse();

        ex.getBindingResult().getAllErrors().forEach((ObjectError error) -> {
            if (error instanceof FieldError errField) {
                errors.addError(errField.getField(), errField.getDefaultMessage());
            }
        });

        return badRequest().body(errors);
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException ex) {
        log.warn("Se recibió una excepción de SQLIntegrityConstraintViolationException, se recomienda implementar el método onSQLIntegrityConstraintViolationException de la clase   GlobalExcepcionHandler para evitar fugas de información del esquema de base de datos: {}", ex.getMessage());
        return badRequest().body(of("Error en la consistencia de datos"));
    }

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotWritableException(HttpMessageNotWritableException ex) {
        return badRequest().body(of("Error al escribir la respuesta: " + ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error(ex);
        return badRequest().body(of("Error en el cuerpo de la petición"));
    }


    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return badRequest().body(of("Método no soportado: " + ex.getMethod()));
    }


    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex) {
        return status(HttpStatus.NOT_FOUND).body(of(ex.getResourcePath(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(IOException ex) {
        return badRequest().body(of("Error de lectura/escritura: " + ex.getMessage()));
    }

}