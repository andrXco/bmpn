package co.edu.javeriana.bmpn.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import co.edu.javeriana.bmpn.dto.error.ErrorDto;
import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.exception.AutenticacionRequeridaException;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> manejarValidacion(
            MethodArgumentNotValidException exception) {
        List<String> detalles = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "SOLICITUD_INVALIDA",
                "Uno o mas campos no cumplen las reglas de validacion",
                detalles);
    }

    @ExceptionHandler(AutenticacionRequeridaException.class)
    public ResponseEntity<ErrorDto> manejarAutenticacion(
            AutenticacionRequeridaException exception) {
        return crearRespuesta(HttpStatus.UNAUTHORIZED, "AUTENTICACION_REQUERIDA",
                exception.getMessage(), List.of());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorDto> manejarAccesoDenegado(
            AccesoDenegadoException exception) {
        return crearRespuesta(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO",
                exception.getMessage(), List.of());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorDto> manejarNoEncontrado(
            RecursoNoEncontradoException exception) {
        return crearRespuesta(HttpStatus.NOT_FOUND, "RECURSO_NO_ENCONTRADO",
                exception.getMessage(), List.of());
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ErrorDto> manejarDuplicado(RecursoDuplicadoException exception) {
        return crearRespuesta(HttpStatus.CONFLICT, "RECURSO_DUPLICADO",
                exception.getMessage(), List.of());
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorDto> manejarOperacionPendiente(
            UnsupportedOperationException exception) {
        return crearRespuesta(HttpStatus.NOT_IMPLEMENTED, "OPERACION_PENDIENTE",
                exception.getMessage(), List.of());
    }

    private ResponseEntity<ErrorDto> crearRespuesta(
            HttpStatus estado,
            String codigo,
            String mensaje,
            List<String> detalles) {
        ErrorDto error = new ErrorDto(codigo, mensaje, Instant.now(), detalles);
        return ResponseEntity.status(estado).body(error);
    }
}
